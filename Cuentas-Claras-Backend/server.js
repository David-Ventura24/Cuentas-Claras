const express = require('express');
const cors = require('cors');
const { createClient } = require('@supabase/supabase-js');
const bcrypt = require('bcryptjs');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());

const supabase = createClient(process.env.SUPABASE_URL, process.env.SUPABASE_KEY);

app.get('/', (req, res) => {
    res.send('Backend de CuentasClarasApp activo');
});

// MIDDLEWARE PARA PROTEGER RUTAS
const verificarToken = (req, res, next) => {
    // Jalamos el token que viene en los Headers (Authorization)
    const authHeader = req.headers['authorization'];
    const token = authHeader && authHeader.split(' ')[1]; // El formato suele ser "Bearer TOKEN"

    if (!token) {
        return res.status(401).json({ error: "Acceso denegado. Token no proporcionado." });
    }

    try {
        const jwt = require('jsonwebtoken');
        // Verificamos si el token es real y no ha expirado
        const verificado = jwt.verify(token, process.env.JWT_SECRET);
        req.usuario = verificado; // Guardamos los datos del usuario en la petición
        next(); // Le damos el pase para que continúe al endpoint real
    } catch (error) {
        return res.status(403).json({ error: "Token inválido o expirado" });
    }
};

// ENDPOINT DE REGISTRO
app.post('/api/auth/register', async (req, res) => {
    const { correo_electronico, nombre, contrasena } = req.body;

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
                    correo_electronico: correo_electronico, 
                    nombre: nombre, 
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
        //  Buscar al usuario en Supabase por su correo
        const { data: usuario, error } = await supabase
            .from('Usuario')
            .select('*')
            .eq('correo_electronico', correo_electronico)
            .single();

        // Si no existe el correo o da error
        if (error || !usuario) {
            return res.status(401).json({ error: "Credenciales incorrectas" });
        }

        //  Comparar la contraseña ingresada con la encriptada en la base de datos
        const contrasenaValida = await bcrypt.compare(contrasena, usuario.contrasena);
        
        if (!contrasenaValida) {
            return res.status(401).json({ error: "Credenciales incorrectas" });
        }

        //  Generar el Token JWT de sesión (Exclusivo para proteger rutas)
        const jwt = require('jsonwebtoken');
        const token = jwt.sign(
            { id: usuario.id, correo: usuario.correo_electronico },
            process.env.JWT_SECRET,
            { expiresIn: '24h' } // El token expira en un día
        );

        //  Responder al cliente con éxito y su token
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

//PANTALLA DE PRESUPUESTO 

app.post('/api/presupuestos', verificarToken, async (req, res) => {
    const usuarioId = req.usuario.id;
    const { cantidad_total, periodo, porcentaje_ahorro } = req.body;

    if (!cantidad_total || !periodo) {
        return res.status(400).json({ error: "La cantidad total y el periodo son obligatorios" });
    }

    const total = parseFloat(cantidad_total);
    // Validación del negocio: El porcentaje va de 5 en 5 hasta el 30%
    const pct = parseFloat(porcentaje_ahorro || 0);
    if (pct < 0 || pct > 30 || pct % 5 !== 0) {
        return res.status(400).json({ error: "El porcentaje de ahorro debe ser entre 0% y 30%, en pasos de 5 en 5." });
    }

    const dineroAhorro = total * (pct / 100); 
    const dineroDisponible = total - dineroAhorro;
    
    // Límite diario sugerido
    let dias = periodo.toLowerCase() === 'semanal' ? 7 : 30;
    const limiteDiario = dineroDisponible / dias;

    try {
        //Buscamos de forma manual si el usuario ya tiene un presupuesto asignado
        const { data: presupuestoExistente, error: errorBusqueda } = await supabase
            .from('Presupuesto')
            .select('id')
            .eq('id_usuario', usuarioId)
            .maybeSingle();

        if (errorBusqueda) return res.status(400).json({ error: errorBusqueda.message });

        let resultado;

        if (presupuestoExistente) {
            //Si ya existe, hacemos un UPDATE tradicional
            const { data, error } = await supabase
                .from('Presupuesto')
                .update({
                    cantidad_total: total,
                    periodo: periodo,
                    ahorro: dineroAhorro,
                    cantidad_disponible: dineroDisponible,
                    dinero_disponible: dineroDisponible,
                    limite_diario: limiteDiario
                })
                .eq('id_usuario', usuarioId)
                .select()
                .single();
            
            if (error) return res.status(400).json({ error: error.message });
            resultado = data;
        } else {
            // Si no existe, hacemos un INSERT tradicional
            const { data, error } = await supabase
                .from('Presupuesto')
                .insert([
                    {
                        id_usuario: usuarioId,
                        cantidad_total: total,
                        periodo: periodo,
                        ahorro: dineroAhorro,
                        cantidad_disponible: dineroDisponible,
                        dinero_disponible: dineroDisponible,
                        limite_diario: limiteDiario
                    }
                ])
                .select()
                .single();

            if (error) return res.status(400).json({ error: error.message });
            resultado = data;
        }

        return res.status(200).json({
            mensaje: "¡Presupuesto guardado con éxito!",
            presupuesto: resultado
        });

    } catch (error) {
        return res.status(500).json({ error: error.message });
    }
});


// PANTALLA DE INICIO (DASHBOARD GENERAL)

app.get('/api/dashboard', verificarToken, async (req, res) => {
    const usuarioId = req.usuario.id;

    try {
        // Jalar presupuesto
        const { data: presupuesto, error: errP } = await supabase
            .from('Presupuesto')
            .select('*')
            .eq('id_usuario', usuarioId)
            .maybeSingle();

        if (errP) return res.status(400).json({ error: errP.message });
        
        // Si no ha configurado presupuesto, mandamos valores en 0 para que la app no se caiga
        if (!presupuesto) {
            return res.status(200).json({
                mensaje: "Falta configurar presupuesto",
                balance_disponible: 0,
                presupuesto_general: 0,
                ahorrado: 0,
                gastos_totales: 0,
                limite_diario_sugerido: 0
            });
        }

        // Surtir la suma de los gastos individuales acumulados
        const { data: gastos, error: errG } = await supabase
            .from('Gastos')
            .select('total_gastado')
            .eq('id_usuario', usuarioId);

        if (errG) return res.status(400).json({ error: errG.message });

        const gastosTotales = gastos.reduce((acc, g) => acc + parseFloat(g.total_gastado || 0), 0);
        // Balance disponible = Dinero inicial libre de ahorro - Todo lo gastado
        const balanceDisponible = presupuesto.dinero_disponible - gastosTotales;

        return res.status(200).json({
            balance_disponible: balanceDisponible,
            presupuesto_general: presupuesto.cantidad_total,
            ahorrado: presupuesto.ahorro,
            gastos_totales: gastosTotales,
            limite_diario_sugerido: presupuesto.limite_diario
        });

    } catch (error) {
        return res.status(500).json({ error: error.message });
    }
});


// PANTALLA DE HISTORIAL (FILTRADO POR MES)


app.get('/api/historial', verificarToken, async (req, res) => {
    const usuarioId = req.usuario.id;
    const { mes, anio } = req.query; 

   try {
        let query = supabase
            .from('Gastos')
            .select('id, total_gastado, categoria, img, fecha')
            .eq('id_usuario', usuarioId)
            .order('fecha', { ascending: false });

        // Si la app solicita un mes específico, calculamos el rango real
        if (mes && anio) {
            const m = parseInt(mes);
            const a = parseInt(anio);

            // Primer día del mes solicitado
            const primerDia = `${a}-${String(m).padStart(2, '0')}-01T00:00:00`;
            
            // Calculamos el primer día del SIGUIENTE mes de forma matemática
            const siguienteMes = m === 12 ? 1 : m + 1;
            const siguienteAnio = m === 12 ? a + 1 : a;
            const limiteSuperior = `${siguienteAnio}-${String(siguienteMes).padStart(2, '0')}-01T00:00:00`;

            // Filtramos: Mayor o igual al inicio de este mes, pero menor al inicio del otro
            query = query.gte('fecha', primerDia).lt('fecha', limiteSuperior);
        }

        const { data: gastos, error } = await query;
        if (error) return res.status(400).json({ error: error.message });

        return res.status(200).json(gastos);
    } catch (error) {
        return res.status(500).json({ error: error.message });
    }
});


// PANTALLA DE GRÁFICA 

app.get('/api/grafica', verificarToken, async (req, res) => {
    const usuarioId = req.usuario.id;

    try {
        const { data: gastos, error } = await supabase
            .from('Gastos')
            .select('total_gastado, categoria')
            .eq('id_usuario', usuarioId);

        if (error) return res.status(400).json({ error: error.message });

        const gastosTotales = gastos.reduce((acc, g) => acc + parseFloat(g.total_gastado || 0), 0);

        // Agrupar gastos por categoría mapeada
        const analisisCategorias = {};
        gastos.forEach(g => {
            const cat = g.categoria || 'Otros';
            const monto = parseFloat(g.total_gastado || 0);
            if (!analisisCategorias[cat]) {
                analisisCategorias[cat] = 0;
            }
            analisisCategorias[cat] += monto;
        });

        // Darle formato estructurado con porcentajes listos para el "Pie Chart"
        const dataGrafica = Object.keys(analisisCategorias).map(cat => {
            const totalCategoria = analisisCategorias[cat];
            const porcentaje = gastosTotales > 0 ? (totalCategoria / gastosTotales) * 100 : 0;
            return {
                categoria: cat,
                total_gastado: totalCategoria,
                porcentaje: parseFloat(porcentaje.toFixed(2)) // Redondeado a dos decimales
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


// PANTALLA DE REGISTRAR GASTO 

app.post('/api/gastos', verificarToken, async (req, res) => {
    const usuarioId = req.usuario.id;
    const { total_gastado, categoria, img, nombre_gasto } = req.body; 
    // Nota: Como en tu tabla guardas la descripción, puedes meter 'nombre_gasto' en la columna descripcion si no la creaste formalmente

    if (!total_gastado || !categoria) {
        return res.status(400).json({ error: "Monto y categoría obligatorios" });
    }

    try {
        const { data, error } = await supabase
            .from('Gastos')
            .insert([
                {
                    id_usuario: usuarioId,
                    total_gastado: parseFloat(total_gastado),
                    categoria: categoria,
                    img: img || null,
                    fecha: new Date()
                    // Si tu tabla tiene la columna descripción para guardar el "nombre", puedes mapearla aquí
                }
            ])
            .select()
            .single();

        if (error) return res.status(400).json({ error: error.message });

        return res.status(201).json({ mensaje: "¡Gasto registrado con éxito!", gasto: data });
    } catch (error) {
        return res.status(500).json({ error: error.message });
    }
});


// SOLICITAR RECUPERACIÓN (GENERAR TOKEN)

app.post('/api/auth/recuperar-password', async (req, res) => {
    const { correo_electronico } = req.body;

    if (!correo_electronico) {
        return res.status(400).json({ error: "El correo electrónico es obligatorio" });
    }

    try {
        //Verificar si el usuario existe
        const { data: usuario, error: errUsuario } = await supabase
            .from('Usuario')
            .select('id')
            .eq('correo_electronico', correo_electronico)
            .maybeSingle();

        if (errUsuario || !usuario) {
            return res.status(404).json({ error: "No existe ningún usuario con ese correo" });
        }

        //  Generar un token aleatorio de 6 dígitos
        const tokenAleatorio = String(Math.floor(100000 + Math.random() * 900000));
        
        // Definir expiración (15 minutos desde ahorita)
        const fechaExpiracion = new Date();
        fechaExpiracion.setMinutes(fechaExpiracion.getMinutes() + 15);

        // Guardar en la tabla token_recuperacion usando los nombres exactos de tu esquema
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

        // En producción se enviaría por correo. 
        return res.status(200).json({
            mensaje: "Código de recuperación generado con éxito (Simulación de envío por correo)",
            codigo_simulado: tokenAleatorio,
            nota: "Ingresa este código en la siguiente pantalla para cambiar tu contraseña."
        });

    } catch (error) {
        return res.status(500).json({ error: error.message });
    }
});


//  RESTABLECER CONTRASEÑA (VALIDAR Y CAMBIAR)

app.post('/api/auth/restablecer-password', async (req, res) => {
    const { correo_electronico, token, nueva_contrasena } = req.body;

    if (!correo_electronico || !token || !nueva_contrasena) {
        return res.status(400).json({ error: "Todos los campos son obligatorios" });
    }

    try {
        // Buscar al usuario primero para tener su ID
        const { data: usuario } = await supabase
            .from('Usuario')
            .select('id')
            .eq('correo_electronico', correo_electronico)
            .maybeSingle();

        if (!usuario) return res.status(404).json({ error: "Usuario no encontrado" });

        // Buscar el token en la tabla y verificar que pertenezca al usuario y no esté usado
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

        //Verificar si el token ya expiró
        const ahora = new Date();
        const fechaExp = new Date(registroToken.expiracion);
        if (ahora > fechaExp) {
            return res.status(400).json({ error: "El código de recuperación ha expirado" });
        }

        //  Encriptar la nueva contraseña con bcrypt
        const bcrypt = require('bcrypt');
        const salt = await bcrypt.genSalt(10);
        const contrasenaEncriptada = await bcrypt.hash(nueva_contrasena, salt);

        // Actualizar la contraseña en la tabla Usuario
        const { error: errUpdateUser } = await supabase
            .from('Usuario')
            .update({ contrasena: contrasenaEncriptada })
            .eq('id', usuario.id);

        if (errUpdateUser) return res.status(400).json({ error: errUpdateUser.message });

        // Marcar el token como usado para que nadie pueda volver a meterlo
        await supabase
            .from('token_recuperacion')
            .update({ usado: true })
            .eq('id', registroToken.id);

        return res.status(200).json({
            mensaje: "Contraseña actualizada con éxito! Ya puedes iniciar sesión con tus nuevas credenciales."
        });

    } catch (error) {
        return res.status(500).json({ error: error.message });
    }
});


app.listen(PORT, () => {
    console.log(`Servidor escuchando en http://localhost:${PORT}`);
});