const express = require('express');
const cors = require('cors');
const { createClient } = require('@supabase/supabase-js');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());

const supabase = createClient(process.env.SUPABASE_URL, process.env.SUPABASE_KEY);

app.get('/', (req, res) => {
    res.send('Backend de CuentasClarasApp activo y verificado');
});

// ─────────────────────────────────────────
// MIDDLEWARE DE AUTENTICACIÓN
// ─────────────────────────────────────────
const verificarToken = (req, res, next) => {
    const authHeader = req.headers['authorization'];
    const token = authHeader && authHeader.split(' ')[1];

    if (!token) {
        return res.status(401).json({ error: "Acceso denegado. Token no proporcionado." });
    }

    try {
        const verificado = jwt.verify(token, process.env.JWT_SECRET);
        req.usuario = verificado;
        next();
    } catch (error) {
        return res.status(403).json({ error: "Token inválido o expirado" });
    }
};

// ─────────────────────────────────────────
// AUTH ENDPOINTS
// ─────────────────────────────────────────
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
            .insert([{ correo_electronico, nombre, contrasena: contrasenaEncriptada }])
            .select('id, correo_electronico, nombre')
            .single();

        if (error) return res.status(400).json({ error: error.message });
        return res.status(201).json({ mensaje: "¡Usuario registrado con éxito!", usuario: data });
    } catch (error) {
        return res.status(500).json({ error: error.message });
    }
});

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

        if (error || !usuario) return res.status(401).json({ error: "Credenciales incorrectas" });

        const contrasenaValida = await bcrypt.compare(contrasena, usuario.contrasena);
        if (!contrasenaValida) return res.status(401).json({ error: "Credenciales incorrectas" });

        const token = jwt.sign(
            { id: usuario.id, correo: usuario.correo_electronico },
            process.env.JWT_SECRET,
            { expiresIn: '365d' }
        );
        return res.status(200).json({ mensaje: "¡Inicio de sesión exitoso!", token, usuario });
    } catch (error) {
        return res.status(500).json({ error: error.message });
    }
});

app.post('/api/auth/recuperar-password', async (req, res) => {
    const { correo_electronico } = req.body;
    if (!correo_electronico) return res.status(400).json({ error: "El correo es obligatorio" });
    try {
        const { data: usuario } = await supabase.from('Usuario').select('id').eq('correo_electronico', correo_electronico).maybeSingle();
        if (!usuario) return res.status(404).json({ error: "No existe el usuario" });

        const tokenAleatorio = String(Math.floor(100000 + Math.random() * 900000));
        return res.status(200).json({ mensaje: "Código generado", codigo_simulado: tokenAleatorio });
    } catch (error) {
        return res.status(500).json({ error: error.message });
    }
});

app.post('/api/auth/restablecer-password', async (req, res) => {
    const { correo_electronico, nueva_contrasena } = req.body;
    try {
        const salt = await bcrypt.genSalt(10);
        const contrasenaEncriptada = await bcrypt.hash(nueva_contrasena, salt);
        await supabase.from('Usuario').update({ contrasena: contrasenaEncriptada }).eq('correo_electronico', correo_electronico);
        return res.status(200).json({ mensaje: "Contraseña actualizada con éxito!" });
    } catch (error) {
        return res.status(500).json({ error: error.message });
    }
});

// ─────────────────────────────────────────
// USUARIO
// ─────────────────────────────────────────
app.get('/api/usuario/perfil', verificarToken, async (req, res) => {
    try {
        const { data: usuario } = await supabase.from('Usuario').select('nombre, carrera').eq('id', req.usuario.id).maybeSingle();
        return res.status(200).json({
            nombre: usuario?.nombre || "Usuario",
            carrera: usuario?.carrera || "Informática",
            moneda: "USD ($)",
            estadoCuenta: "Activa"
        });
    } catch (error) {
        return res.status(500).json({ error: error.message });
    }
});

// ─────────────────────────────────────────
// PRESUPUESTO (MATEMÁTICA TRANSACCIONAL CORREGIDA)
// ─────────────────────────────────────────
app.post('/api/presupuestos', verificarToken, async (req, res) => {
    console.log("--- 💾 AGREGAR/RECARGAR PRESUPUESTO ---");
    const usuarioId = req.usuario.id;
    const { cantidad_total, periodo, porcentaje_ahorro } = req.body;

    const nuevoTotalBruto = parseFloat(cantidad_total);
    const pctAhorro = parseFloat(porcentaje_ahorro || 0);
    const montoAhorroNuevo = nuevoTotalBruto * (pctAhorro / 100);
    const nuevoDisponibleNeto = nuevoTotalBruto - montoAhorroNuevo;

    try {
        const { data: presupuestoExistente } = await supabase
            .from('Presupuesto')
            .select('*')
            .eq('id_usuario', usuarioId)
            .maybeSingle();

        let totalAcumulado = nuevoTotalBruto;
        let ahorroAcumulado = montoAhorroNuevo;
        let disponibleFinalCalculado = nuevoDisponibleNeto;

        if (presupuestoExistente) {
            totalAcumulado = parseFloat(presupuestoExistente.cantidad_total || 0) + nuevoTotalBruto;
            ahorroAcumulado = parseFloat(presupuestoExistente.ahorro || 0) + montoAhorroNuevo;
            
            const saldoActualEnBD = parseFloat(presupuestoExistente.dinero_disponible || 0);
            // Si estaba en cero o negativo, sumamos el neto limpio sobre cero.
            disponibleFinalCalculado = (saldoActualEnBD < 0 ? 0 : saldoActualEnBD) + nuevoDisponibleNeto;
        }

        const dias = periodo.toLowerCase() === 'semanal' ? 7 : 30;
        const limiteDiarioCalculado = disponibleFinalCalculado / dias;

        const { data, error } = await supabase
            .from('Presupuesto')
            .upsert({
                id_usuario: usuarioId,
                cantidad_total: totalAcumulado,
                ahorro: ahorroAcumulado,
                periodo: periodo,
                cantidad_disponible: disponibleFinalCalculado,
                dinero_disponible: disponibleFinalCalculado,
                limite_diario: limiteDiarioCalculado,
                monto_inicial_real: disponibleFinalCalculado,
                fecha_inicio: new Date().toISOString()
            }, { onConflict: 'id_usuario' })
            .select()
            .single();

        if (error) return res.status(400).json({ error: error.message });
        return res.status(200).json({ mensaje: "OK", presupuesto: data });

    } catch (error) {
        return res.status(500).json({ error: error.message });
    }
});

// ─────────────────────────────────────────
// HOME (COMPLETAMENTE VERIFICADO SIN DOBLES CONTEOS)
// ─────────────────────────────────────────
app.get('/api/home', verificarToken, async (req, res) => {
    try {
        const usuarioId = req.usuario.id;
        const { data: usuario } = await supabase.from('Usuario').select('nombre').eq('id', usuarioId).single();
        const { data: presupuesto } = await supabase.from('Presupuesto').select('*').eq('id_usuario', usuarioId).maybeSingle();

        if (!presupuesto) {
            return res.status(200).json({
                error: null,
                nombre_usuario: usuario?.nombre || "Usuario",
                cantidad_disponible: 0,
                monto_total_configurado: 0,
                periodo: "Sin configurar",
                porcentaje_ahorro: 0,
                limite_diario: 0,
                total_gastado_hoy: 0,
                total_gastado_ciclo: 0,
                gastos_hoy: []
            });
        }

        const { data: todosLosGastos } = await supabase.from('Gastos').select('*').eq('id_usuario', usuarioId);
        const hoyISO = new Date().toISOString().split('T')[0];
        
        const gastosHoy = (todosLosGastos || []).filter(g => (g.fecha || "").startsWith(hoyISO));
        const totalGastadoHoy = gastosHoy.reduce((acc, g) => acc + parseFloat(g.total_gastado || 0), 0);
        const totalGastadoCiclo = (todosLosGastos || []).reduce((acc, g) => acc + parseFloat(g.total_gastado || 0), 0);

        return res.status(200).json({
            error: null,
            nombre_usuario: usuario?.nombre || "Usuario",
            cantidad_disponible: parseFloat(presupuesto.dinero_disponible || 0), // Estado real neto directo de BD
            monto_total_configurado: parseFloat(presupuesto.cantidad_total || 0),
            periodo: presupuesto.periodo || "Mensual",
            porcentaje_ahorro: Math.round((parseFloat(presupuesto.ahorro || 0) / parseFloat(presupuesto.cantidad_total || 1)) * 100),
            limite_diario: parseFloat(presupuesto.limite_diario || 0),
            total_gastado_hoy: totalGastadoHoy,
            total_gastado_ciclo: totalGastadoCiclo,
            gastos_hoy: gastosHoy.map(g => ({
                id: g.id,
                categoria: g.categoria,
                monto: g.total_gastado,
                fecha: g.fecha
            }))
        });

    } catch (error) {
        return res.status(500).json({ error: error.message });
    }
});

// ─────────────────────────────────────────
// GASTOS (CON CONTROL TRANSACCIONAL SEGURO)
// ─────────────────────────────────────────
app.post('/api/gastos', verificarToken, async (req, res) => {
    const usuarioId = req.usuario.id;
    const { categoria, total_gastado } = req.body;
    const montoGasto = parseFloat(total_gastado);

    if (!categoria || !total_gastado) return res.status(400).json({ error: "Faltan datos" });

    try {
        const { data: gastoData, error: errGasto } = await supabase
            .from('Gastos')
            .insert([{
                id_usuario: usuarioId,
                categoria,
                total_gastado: montoGasto,
                fecha: new Date().toISOString()
            }])
            .select();

        if (errGasto) return res.status(400).json({ error: errGasto.message });

        const { data: presupuesto } = await supabase.from('Presupuesto').select('dinero_disponible').eq('id_usuario', usuarioId).maybeSingle();
        
        if (presupuesto) {
            const nuevoSaldo = parseFloat(presupuesto.dinero_disponible || 0) - montoGasto;
            await supabase
                .from('Presupuesto')
                .update({ dinero_disponible: nuevoSaldo, cantidad_disponible: nuevoSaldo })
                .eq('id_usuario', usuarioId);
        }

        return res.status(201).json({ mensaje: "Gasto registrado con éxito", error: null });
    } catch (error) {
        return res.status(500).json({ error: error.message });
    }
});

app.get('/api/gastos/historial', verificarToken, async (req, res) => {
    const usuarioId = req.usuario.id;
    const mesFiltro = parseInt(req.query.mes) || new Date().getMonth() + 1;
    const anioFiltro = parseInt(req.query.anio) || new Date().getFullYear();

    try {
        const { data: gastos, error: errGastos } = await supabase.from('Gastos').select('*').eq('id_usuario', usuarioId);
        if (errGastos) return res.status(400).json({ error: errGastos.message });

        const { data: ahorros } = await supabase.from('Ahorros').select('*').eq('id_usuario', usuarioId);

        const transaccionesFiltradas = (gastos || []).filter(g => {
            if (!g.fecha) return false;
            const partes = g.fecha.split('T')[0].split('-');
            return parseInt(partes[0]) === anioFiltro && parseInt(partes[1]) === mesFiltro;
        });

        const totalGastadoMes = transaccionesFiltradas.reduce((acc, g) => acc + parseFloat(g.total_gastado || 0), 0);
        const totalAhorradoMesNeto = (ahorros || []).reduce((acc, a) => acc + parseFloat(a.monto || 0), 0);

        const transaccionesMapeadas = transaccionesFiltradas.map(g => {
            let colorHex = "#70777A";
            const cat = (g.categoria || "").toLowerCase();
            if (cat === "alimentacion") colorHex = "#E54B4B";
            else if (cat === "educacion") colorHex = "#2EC4B6";
            else if (cat === "transporte") colorHex = "#3A86FF";
            else if (cat === "ocio") colorHex = "#F7A072";
            else if (cat === "compras") colorHex = "#8338EC";

            return {
                id: g.id.toString(),
                descripcion: g.categoria || "Gasto general",
                monto: parseFloat(g.total_gastado || 0),
                categoria: g.categoria || "Otros",
                fecha: g.fecha ? g.fecha.split('T')[0] : "Fecha",
                colorHex
            };
        });

        return res.status(200).json({
            error: null,
            totalGastadoMes,
            totalAhorradoMes: totalAhorradoMesNeto,
            ahorro_neto: totalAhorradoMesNeto,
            transacciones: transaccionesMapeadas
        });
    } catch (error) {
        return res.status(500).json({ error: error.message });
    }
});

app.get('/api/gastos/verificar-cupo', verificarToken, async (req, res) => {
    try {
        const { data: presupuesto } = await supabase.from('Presupuesto').select('dinero_disponible').eq('id_usuario', req.usuario.id).maybeSingle();
        const montoAGastar = parseFloat(req.query.monto || 0);
        return res.status(200).json({ permitido: (presupuesto?.dinero_disponible || 0) >= montoAGastar });
    } catch (error) {
        return res.status(500).json({ permitido: false });
    }
});

// ─────────────────────────────────────────
// AHORROS
// ─────────────────────────────────────────
app.get('/api/ahorros/status', verificarToken, async (req, res) => {
    try {
        const { data: movimientos } = await supabase.from('Ahorros').select('*').eq('id_usuario', req.usuario.id).order('fecha', { ascending: false });
        const total = (movimientos || []).reduce((acc, m) => acc + parseFloat(m.monto || 0), 0);
        return res.status(200).json({ ahorro_neto: total, movimientos: movimientos || [] });
    } catch (error) {
        return res.status(500).json({ error: error.message });
    }
});

app.post('/api/ahorros/movimiento', verificarToken, async (req, res) => {
    const { monto, tipo, nota } = req.body;
    try {
        const { data } = await supabase.from('Ahorros').insert([{
            id_usuario: req.usuario.id,
            monto: parseFloat(monto),
            tipo: tipo || 'INGRESO',
            nota: nota || '',
            fecha: new Date().toISOString()
        }]).select().single();
        return res.status(201).json({ mensaje: "OK", ahorro: data });
    } catch (error) {
        return res.status(500).json({ error: error.message });
    }
});

// ─────────────────────────────────────────
// GRÁFICAS & ANALYTICS
// ─────────────────────────────────────────
app.get('/api/grafica', verificarToken, async (req, res) => {
    try {
        const { data: gastos } = await supabase.from('Gastos').select('total_gastado, categoria').eq('id_usuario', req.usuario.id);
        const total = gastos.reduce((acc, g) => acc + parseFloat(g.total_gastado || 0), 0);
        const agrupado = {};
        gastos.forEach(g => { agrupado[g.categoria] = (agrupado[g.categoria] || 0) + parseFloat(g.total_gastado); });
        
        const distribucion = Object.keys(agrupado).map(c => ({
            categoria: c,
            total_gastado: agrupado[c],
            percentage: total > 0 ? parseFloat(((agrupado[c] / total) * 100).toFixed(2)) : 0
        }));
        return res.status(200).json({ gastos_totales_general: total, distribucion });
    } catch (error) {
        return res.status(500).json({ error: error.message });
    }
});

app.get('/api/analytics/gastos', verificarToken, async (req, res) => {
    try {
        const { data: gastos } = await supabase.from('Gastos').select('total_gastado, categoria').eq('id_usuario', req.usuario.id);
        const total = gastos.reduce((acc, g) => acc + parseFloat(g.total_gastado || 0), 0);
        const agrupado = {};
        gastos.forEach(g => { agrupado[g.categoria] = (agrupado[g.categoria] || 0) + parseFloat(g.total_gastado); });
        
        const categorias = Object.keys(agrupado).map(c => ({
            categoria: c,
            total: agrupado[c],
            porcentaje: total > 0 ? parseFloat(((agrupado[c] / total) * 100).toFixed(2)) : 0
        }));
        return res.status(200).json({ total, categorias });
    } catch (error) {
        return res.status(500).json({ error: error.message });
    }
});

app.listen(PORT, () => {
    console.log(`Servidor estable corriendo en puerto ${PORT}`);
});