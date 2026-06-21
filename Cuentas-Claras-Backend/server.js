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

app.listen(PORT, () => {
    console.log(`Servidor escuchando en http://localhost:${PORT}`);
});