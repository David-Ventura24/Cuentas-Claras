const express = require('express');
const cors = require('cors');
const { createClient } = require('@supabase/supabase-js');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken'); // Importado arriba de forma limpia
require('dotenv').config();

const { Resend } = require('resend');
const resend = new Resend(process.env.RESEND_API_KEY);

const app = express();
const PORT = process.env.PORT || 3000;


app.use(cors());
app.use(express.json());

const supabase = createClient(process.env.SUPABASE_URL, process.env.SUPABASE_KEY);

const nodemailer = require('nodemailer');

const transporter = nodemailer.createTransport({
    host: 'smtp.resend.com',
    port: 465,
    secure: true,
    auth: {
        user: 'resend', // Literalmente la palabra 'resend'
        pass: process.env.RESEND_API_KEY // Tu API key de Resend
    }
});



app.get('/', (req, res) => {
    res.send('Backend de CuentasClarasApp activo');
});

// MIDDLEWARE PARA PROTEGER RUTAS
const verificarToken = (req, res, next) => {
    const authHeader = req.headers['authorization'];
    
    //  LOG DE DETECTIVE
    console.log("--- NUEVA PETICIÓN ---");
    console.log("Ruta:", req.path);
    console.log("Cabecera Authorization recibida:", authHeader);

    const token = authHeader && authHeader.split(' ')[1];

    if (!token) {
        console.log("❌ ERROR 401: No se envió ningún token.");
        return res.status(401).json({ error: "Acceso denegado. Token no proporcionado." });
    }

    try {
        const verificado = jwt.verify(token, process.env.JWT_SECRET);
        req.usuario = verificado; 
        console.log("✅ Token válido para el usuario ID:", verificado.id);
        next(); 
    } catch (error) {
        console.log("❌ ERROR 403: Token inválido o expirado.", error.message);
        return res.status(403).json({ error: "Token inválido o expirado" });
    }
};

// ENDPOINT DE REGISTRO
app.post('/api/auth/register', async (req, res) => {
    const { correo_electronico, nombre, contrasena} = req.body; // Añadido carrera opcional

    if (!correo_electronico || !nombre || !contrasena) {
        return res.status(400).json({ error: "Todos los campos son obligatorios" });
    }

    try {
        const salt = await bcrypt.genSalt(10);
        const contrasenaEncriptada = await bcrypt.hash(contrasena, salt);

        const { data, error } = await supabase
            .from('Usuario') 
            .insert([
                { 
                    correo_electronico, 
                    nombre, 
                    contrasena: contrasenaEncriptada

                }
            ])
            .select('id, correo_electronico, nombre')
            .single();

        if (error) return res.status(400).json({ error: error.message });

        return res.status(201).json({
            mensaje: "¡Usuario registrado con éxito!",
            usuario: data
        });

    } catch (error) {
        return res.status(500).json({ error: error.message });
    }
});

// ENDPOINT DE LOGIN
app.post('/api/auth/login', async (req, res) => {
    const { correo_electronico, contrasena } = req.body;

    if (!correo_electronico || !contrasena) {
        return res.status(400).json({ error: "Correo y contraseña son obligatorios" });
    }

    try {
        const { data: usuario, error } = await supabase
            .from('Usuario')
            .select('*')
            .eq('correo_electronico', correo_electronico)
            .single();

        if (error || !usuario) {
            return res.status(401).json({ error: "Credenciales incorrectas" });
        }

        const contrasenaValida = await bcrypt.compare(contrasena, usuario.contrasena);
        if (!contrasenaValida) {
            return res.status(401).json({ error: "Credenciales incorrectas" });
        }

        const token = jwt.sign(
            { id: usuario.id, correo: usuario.correo_electronico },
            process.env.JWT_SECRET,
            { expiresIn: '365d' }
        );

        return res.status(200).json({
            mensaje: "¡Inicio de sesión exitoso!",
            token: token,
            usuario: {
                id: usuario.id,
                nombre: usuario.nombre,
                correo_electronico: usuario.correo_electronico
            }
        });

    } catch (error) {
        return res.status(500).json({ error: error.message });
    }
});

app.post('/api/presupuestos', verificarToken, async (req, res) => {
    const usuarioId = req.usuario.id;
    const { cantidad_total, periodo, porcentaje_ahorro } = req.body;
    
    const totalRecibido = parseFloat(cantidad_total);
    const pctNuevo = parseFloat(porcentaje_ahorro || 0);

    try {
        const { data: ex } = await supabase.from('Presupuesto')
            .select('*').eq('id_usuario', usuarioId).maybeSingle();

        let montoInyectado = totalRecibido;
        let ahorroExistente = 0;
        let disponibleExistente = 0;
        let cantidadTotalExistente = 0;
        let fechaActualizacion = new Date();

        if (ex) {
            disponibleExistente = parseFloat(ex.dinero_disponible || 0);

            if (disponibleExistente <= 0) {
                // Si el balance llegó a 0, ignoramos el total anterior y empezamos de cero
                montoInyectado = totalRecibido;
                ahorroExistente = 0;
                disponibleExistente = 0;
                cantidadTotalExistente = 0;
                fechaActualizacion = new Date();
            } else {
                // Si aún queda dinero, el nuevo monto se SUMA al existente
                // (totalRecibido es el monto que el usuario está agregando, no el nuevo total acumulado)
                montoInyectado = totalRecibido;
                ahorroExistente = parseFloat(ex.ahorro || 0);
                cantidadTotalExistente = parseFloat(ex.cantidad_total || 0);
                fechaActualizacion = ex.updated_at;
            }
        }

        // 2. Cálculo Incremental
        const ahorroDeLaInyeccion = montoInyectado * (pctNuevo / 100);
        const disponibleDeLaInyeccion = montoInyectado - ahorroDeLaInyeccion;

        const nuevoAhorroTotal = ahorroExistente + ahorroDeLaInyeccion;
        const nuevoDisponibleTotal = disponibleExistente + disponibleDeLaInyeccion;
        const nuevaCantidadTotal = cantidadTotalExistente + montoInyectado;
        
        const dias = periodo.toLowerCase() === 'semanal' ? 7 : 30;

        // 3. Guardar con la matemática corregida
        const { data: presupuesto, error } = await supabase
            .from('Presupuesto')
            .upsert({
                id_usuario: usuarioId,
                cantidad_total: nuevaCantidadTotal, 
                ahorro: nuevoAhorroTotal,      
                periodo: periodo,
                cantidad_disponible: nuevoDisponibleTotal,
                dinero_disponible: nuevoDisponibleTotal,
                limite_diario: nuevoDisponibleTotal / dias,
                updated_at: fechaActualizacion,
                monto_inicial_real: nuevoDisponibleTotal
            }, { onConflict: 'id_usuario' })
            .select().single();

        if (error) throw error;

        //  Usamos ahorroDeLaInyeccion
        if (ahorroDeLaInyeccion > 0) {
            await supabase.from('Ahorros').insert([{
                id_usuario: usuarioId,
                monto: ahorroDeLaInyeccion,
                tipo: 'INGRESO',
                nota: `Ahorro automático por inyección`,
                fecha: new Date().toISOString() 
            }]);
        }

        res.json({ mensaje: "OK", presupuesto });
    } catch (error) { res.status(400).json({ error: error.message }); }
});

// 2. PANTALLA HOME (CORREGIDA PARA ENVIAR BRUTOS)
app.get('/api/home', verificarToken, async (req, res) => {
    try {
        const usuarioId = req.usuario.id;
        const { data: usuario } = await supabase.from('Usuario').select('nombre').eq('id', usuarioId).single();
        const { data: presupuesto } = await supabase.from('Presupuesto').select('*').eq('id_usuario', usuarioId).maybeSingle();

        if (!presupuesto) return res.status(200).json({ nombre_usuario: usuario?.nombre || "Usuario", cantidad_disponible: 0, gastos_hoy: [] });

        const fechaRef = new Date(presupuesto.updated_at).getTime();
        const { data: todosLosGastos } = await supabase.from('Gastos').select('*').eq('id_usuario', usuarioId);
        
        const gastosCiclo = (todosLosGastos || []).filter(g => new Date(g.fecha).getTime() >= fechaRef);
        const totalGastadoCiclo = gastosCiclo.reduce((acc, g) => acc + parseFloat(g.total_gastado || 0), 0);
        
        const disponibleBruto = parseFloat(presupuesto.cantidad_disponible || 0);
        const balanceReal = disponibleBruto - totalGastadoCiclo;

        res.json({
            nombre_usuario: usuario?.nombre || "Usuario",
            cantidad_disponible: Math.max(0, balanceReal), 
            // 🌟 CAMBIO: En lugar de mandar el Bruto (200), mandamos el Disponible Inicial (150)
            monto_total_configurado: disponibleBruto, 
            periodo: presupuesto.periodo,
            porcentaje_ahorro: Math.round((parseFloat(presupuesto.ahorro)/parseFloat(presupuesto.cantidad_total))*100),
            limite_diario: parseFloat(presupuesto.limite_diario || 0),
            total_gastado_hoy: (todosLosGastos || []).filter(g => g.fecha.startsWith(new Date().toISOString().split('T')[0])).reduce((acc,g)=> acc + parseFloat(g.total_gastado), 0),
            gastos_hoy: gastosCiclo.map(g => ({ id: g.id, categoria: g.categoria, monto: g.total_gastado, fecha: g.fecha }))
        });
    } catch (error) { res.status(500).json({ error: error.message }); }
});

// OBTENER STATUS DE AHORROS (Para la pantalla Savings)
app.get('/api/ahorros/status', verificarToken, async (req, res) => {
    const usuarioId = req.usuario.id;
    try {
        const { data: movimientos } = await supabase.from('Ahorros')
            .select('*').eq('id_usuario', usuarioId).order('fecha', { ascending: false });
        
        const total = (movimientos || []).reduce((acc, m) => 
            m.tipo === 'INGRESO' ? acc + m.monto : acc - m.monto, 0);

        res.json({ ahorro_neto: total, movimientos: movimientos || [] });
    } catch (error) { res.status(500).json({ error: error.message }); }
});




// PANTALLA DE HISTORIAL (FILTRADO DINÁMICO POR MES Y AÑO) 
app.get('/api/gastos/historial', verificarToken, async (req, res) => {
    const usuarioId = req.usuario.id;
    // Android mandará por ejemplo: /api/gastos/historial?mes=5&anio=2026
    const mesFiltro = parseInt(req.query.mes) || new Date().getMonth() + 1;
    const anioFiltro = parseInt(req.query.anio) || new Date().getFullYear();

    try {
        const { data: gastos, error } = await supabase
            .from('Gastos')
            .select('*')
            .eq('id_usuario', usuarioId);

        if (error) return res.status(400).json({ error: error.message });

        // Filtrar en memoria para evitar lidiar con zonas horarias complejas de Postgres de golpe
        const transaccionesFiltradas = gastos.filter(g => {
            if (!g.fecha) return false;
            const fechaObj = new Date(g.fecha);
            // sumamos 1 al mes si Date maneja base 0 o extraemos directo de la cadena ISO
            const partes = g.fecha.split('T')[0].split('-');
            return parseInt(partes[0]) === anioFiltro && parseInt(partes[1]) === mesFiltro;
        });

        const totalGastadoMes = transaccionesFiltradas.reduce((acc, g) => acc + parseFloat(g.total_gastado || 0), 0);

        // Mapeamos los campos tal cual los necesita el HistoryUiState de Android
        const transaccionesMapeadas = transaccionesFiltradas.map(g => {
            // Asignación de colores para las categorías en Android (puedes enviar el HEX)
            let colorHex = "#70777A"; // Otros por defecto
            const cat = (g.categoria || "").toLowerCase();
            if (cat === "alimentacion" || cat === "comida") colorHex = "#E54B4B";
            else if (cat === "educacion") colorHex = "#2EC4B6";
            else if (cat === "transporte") colorHex = "#3A86FF";
            else if (cat === "ahorro") colorHex = "#8338EC";
            else if (cat === "ocio") colorHex = "#F7A072";

            return {
                id: g.id.toString(),
                descripcion: g.categoria ? g.categoria.replaceFirstChar?.({uppercase: true}) || g.categoria : "Gasto general",
                monto: parseFloat(g.total_gastado || 0),
                categoria: g.categoria || "Otros",
                fecha: g.fecha ? g.fecha.split('T')[0] : "Fecha desconocida",
                colorHex: colorHex // El cliente parsea el String hexadecimal a Color() de Compose
            };
        });

        return res.status(200).json({
            error: null,
            totalGastadoMes: totalGastadoMes,
            transacciones: transaccionesMapeadas
        });

    } catch (error) {
        return res.status(500).json({ error: error.message });
    }
});

// PANTALLA DE GRÁFICA (DONUT CANVAS)
app.get('/api/grafica', verificarToken, async (req, res) => {
    const usuarioId = req.usuario.id;

    try {
        const { data: gastos, error } = await supabase
            .from('Gastos')
            .select('total_gastado, categoria')
            .eq('id_usuario', usuarioId);

        if (error) return res.status(400).json({ error: error.message });

        const gastosTotales = gastos.reduce((acc, g) => acc + parseFloat(g.total_gastado || 0), 0);

        const analisisCategorias = {};
        gastos.forEach(g => {
            const cat = g.categoria || 'Otros';
            const monto = parseFloat(g.total_gastado || 0);
            if (!analisisCategorias[cat]) {
                analisisCategorias[cat] = 0;
            }
            analisisCategorias[cat] += monto;
        });

        const dataGrafica = Object.keys(analisisCategorias).map(cat => {
            const totalCategoria = analisisCategorias[cat];
            const porcentaje = gastosTotales > 0 ? (totalCategoria / gastosTotales) * 100 : 0;
            return {
                categoria: cat,
                total_gastado: totalCategoria,
                porcentaje: parseFloat(porcentaje.toFixed(2))
            };
        });

        return res.status(200).json({
            gastos_totales_general: gastosTotales,
            distribucion: dataGrafica
        });

    } catch (error) {
        return res.status(500).json({ error: error.message });
    }
});

// REGISTRAR GASTO REAL DESDE EL FORMULARIO (PROTEGIDO)
app.post('/api/gastos', verificarToken, async (req, res) => {
    try {
        const { categoria, total_gastado } = req.body;
        if (!categoria || !total_gastado) return res.status(400).json({ error: "Faltan datos" });

        const { data, error } = await supabase.from('Gastos').insert([
            { id_usuario: req.usuario.id, categoria, total_gastado: parseFloat(total_gastado), fecha: new Date().toISOString() }
        ]).select();

        if (error) return res.status(400).json({ error: error.message });
        return res.status(201).json({ 
            mensaje: "Gasto registrado",
            error: null 
        });
    } catch (error) { res.status(500).json({ error: error.message }); }
});

// VERIFICACIÓN DE CUPO DISPONIBLE
app.get('/api/gastos/verificar-cupo', verificarToken, async (req, res) => {
    const usuarioId = req.usuario.id;
    const montoAGastar = parseFloat(req.query.monto || 0);

    try {
        const { data: presupuesto } = await supabase
            .from('Presupuesto')
            .select('dinero_disponible')
            .eq('id_usuario', usuarioId)
            .maybeSingle();

        const { data: gastos } = await supabase
            .from('Gastos')
            .select('total_gastado')
            .eq('id_usuario', usuarioId);

        const totalGastado = (gastos || []).reduce((acc, g) => acc + parseFloat(g.total_gastado || 0), 0);
        const dineroDisponibleReal = (presupuesto?.dinero_disponible || 0) - totalGastado;

        return res.status(200).json({ permitido: dineroDisponibleReal >= montoAGastar });
    } catch (error) {
        return res.status(500).json({ permitido: false, error: error.message });
    }
});


// SOLICITAR RECUPERACIÓN DE CONTRASEÑA
app.post('/api/auth/recuperar-password', async (req, res) => {
    const { correo_electronico } = req.body;

    if (!correo_electronico) {
        return res.status(400).json({ error: "El correo electrónico es obligatorio" });
    }

    try {
        const { data: usuario, error: errUsuario } = await supabase
            .from('Usuario')
            .select('id')
            .eq('correo_electronico', correo_electronico)
            .maybeSingle();

        if (errUsuario || !usuario) {
            return res.status(404).json({ error: "No existe ningún usuario con ese correo" });
        }

        const tokenAleatorio = String(Math.floor(100000 + Math.random() * 900000));
        const fechaExpiracion = new Date();
        fechaExpiracion.setMinutes(fechaExpiracion.getMinutes() + 15);

        const { error: errToken } = await supabase
            .from('token_recuperacion')
            .insert([
                {
                    id_usuario: usuario.id,
                    token: tokenAleatorio,
                    expiracion: fechaExpiracion,
                    usado: false
                }
            ]);

        if (errToken) return res.status(400).json({ error: errToken.message });

        const mailOptions = {
            from:'onboarding@resend.dev',
            to: correo_electronico,
            subject: 'Código de recuperación - Cuentas Claras',
            text: `Tu código de recuperación es: ${tokenAleatorio}.\nExpira en 15 minutos.`
        };

        // 🌟 CAMBIO: Envolvemos el envío en su propio bloque para capturar errores de SMTP
        try {
            const info = await transporter.sendMail(mailOptions);
            console.log("✅ Correo enviado con éxito:", info.messageId);
        } catch (mailError) {
            console.error("❌ Error directo al enviar el correo con Nodemailer:", mailError);
            return res.status(500).json({ error: "El token se generó, pero falló el envío del correo electrónico." });
        }

        return res.status(200).json({
            mensaje: "El código de recuperación ha sido enviado a tu correo electrónico."
        });

    } catch (error) {
        return res.status(500).json({ error: error.message });
    }
});

// RESTABLECER CONTRASEÑA
app.post('/api/auth/restablecer-password', async (req, res) => {
    const { correo_electronico, token, nueva_contrasena } = req.body;

    if (!correo_electronico || !token || !nueva_contrasena) {
        return res.status(400).json({ error: "Todos los campos son obligatorios" });
    }

    try {
        const { data: usuario } = await supabase
            .from('Usuario')
            .select('id')
            .eq('correo_electronico', correo_electronico)
            .maybeSingle();

        if (!usuario) return res.status(404).json({ error: "Usuario no encontrado" });

        const { data: registroToken, error: errT } = await supabase
            .from('token_recuperacion')
            .select('*')
            .eq('id_usuario', usuario.id)
            .eq('token', token)
            .eq('usado', false)
            .maybeSingle();

        if (errT || !registroToken) {
            return res.status(400).json({ error: "El código es inválido o ya fue utilizado" });
        }

        const ahora = new Date();
        if (ahora > new Date(registroToken.expiracion)) {
            return res.status(400).json({ error: "El código de recuperación ha expirado" });
        }

        const salt = await bcrypt.genSalt(10);
        const contrasenaEncriptada = await bcrypt.hash(nueva_contrasena, salt);

        const { error: errUpdateUser } = await supabase
            .from('Usuario')
            .update({ contrasena: contrasenaEncriptada })
            .eq('id', usuario.id);

        if (errUpdateUser) return res.status(400).json({ error: errUpdateUser.message });

        await supabase
            .from('token_recuperacion')
            .update({ usado: true })
            .eq('id', registroToken.id);

        return res.status(200).json({
            mensaje: "Contraseña actualizada con éxito!"
        });

    } catch (error) {
        return res.status(500).json({ error: error.message });
    }
});


// OBTENER PERFIL DEL USUARIO
app.get('/api/usuario/perfil', verificarToken, async (req, res) => {
    const usuarioId = req.usuario.id;
    console.log("--- 👤 PIDIENDO PERFIL ---");
    console.log("ID del Usuario:", usuarioId);

    try {
        // Intentamos pedir los datos
        const { data: usuario, error } = await supabase
            .from('Usuario')
            .select('nombre, carrera') 
            .eq('id', usuarioId)
            .maybeSingle();

        
        return res.status(200).json({
            nombre: usuario.nombre,
            carrera: usuario.carrera || "Carrera no definida",
            moneda: "USD ($)",
            estadoCuenta: "Activa"
        });

    } catch (error) {
        console.log("🚨 Fallo crítico en Perfil:", error.message);
        return res.status(500).json({ error: error.message });
    }
});

// OBTENER DATOS DE AHORRO ACUMULADO
app.get('/api/ahorros/status', verificarToken, async (req, res) => {
    const usuarioId = req.usuario.id;
    const { data: presupuestos } = await supabase.from('Presupuesto').select('ahorro').eq('id_usuario', usuarioId);
    const totalAhorrado = (presupuestos || []).reduce((acc, p) => acc + parseFloat(p.ahorro || 0), 0);
    res.json({ ahorro_neto: totalAhorrado, movimientos: [] });
});

// OBTENER DISTRIBUCIÓN PARA GRÁFICAS (ANALYTICS)
app.get('/api/analytics/gastos', verificarToken, async (req, res) => {
    const usuarioId = req.usuario.id;
    const { data: gastos } = await supabase.from('Gastos').select('total_gastado, categoria').eq('id_usuario', usuarioId);
    // Agrupar por categoría... (lógica similar a la que ya tienes en server.js para la gráfica)
    res.json({ total: 0, categorias: [] }); 
});


app.listen(PORT, () => {
    console.log(`Servidor activo y corriendo en el puerto ${PORT}`);
});