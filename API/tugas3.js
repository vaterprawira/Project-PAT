const express = require('express'); //import library buat app web
const bodyParser = require('body-parser'); //import library untuk parsing body dari request http
const mysql = require('mysql'); //import library untuk interaksi dengan database mysql
const multer = require('multer'); //import library untuk upload file
const path = require('path');
const { register } = require('module');

const app = express();
app.use(bodyParser.json()); //parsing body request sebagai json
app.use(bodyParser.urlencoded({ extended: true })); //parsing body request sebagai urlencoded


// Konfigurasi penyimpanan multer
const storage = multer.diskStorage({
    destination: function (req, file, cb) {
        cb(null, 'uploads/'); // Direktori tempat menyimpan gambar
    },
    filename: function (req, file, cb) {
        cb(null, Date.now() + path.extname(file.originalname)); // Menambahkan timestamp pada nama file
    }
});

//membuat multer untuk upload file
const upload = multer({ storage: storage });

//membuat konfigurasi untuk koneksi dengan database mysql
const conn = mysql.createConnection({
    host: '127.0.0.1',
    user: 'root',
    password: '',
    database: 'pat',
});

//melakukan koneksi dengan database mysql 
conn.connect(function (err) {
    //jika ada kesalahan saat melakukan koneksi
    if (err) {
        console.error('Error connecting to MySQL: ', err);
        return;
    }
    //jika berhasil melakukan koneksi
    console.log('Connected to MySQL ....');
});

//login 
app.post('/api/pat/login', function(req, res) {
    let username = req.body.username;
    let password = req.body.pass;
    
    let sql = "SELECT * FROM register WHERE username = ?";
    conn.query(sql, [username], function(err, result) {
        if (err) {
            console.error('Error querying database: ', err);
            res.status(500).json({ error: 'Internal server error' });
            return;
        }

        if (result.length > 0) {
            let user = result[0];
            if (user.pass === password) {
                res.status(200).json({ 
                    message: 'Login successful',
                    registerId: user.id // Return the registerId
                });
            } else {
                res.status(401).json({ error: 'Invalid username or password' });
            }
        } else {
            res.status(401).json({ error: 'Invalid username or password' });
        }
    });
});



//register
app.post('/api/pat/register/', function (req, res) {
    let data = {
        username: req.body.username,
        pass: req.body.pass,
        nama: req.body.nama,
        email: req.body.email,
        telepon: req.body.telepon
    };

    let sql = "INSERT INTO register SET ?";
    conn.query(sql, data, function (err, result) {
        if (err) {
            console.error('Error inserting data: ', err);
            res.status(500).json({ error: 'Internal server error' });
            return;
        }
        res.json({
            "status": 200,
            "error": null,
            "response": result
        });
    });
});


//menampilkan data user berdasarkan id di mobile client
app.get('/api/pat/register/:id', function(req, res) {
    const userId = parseInt(req.params.id);
    if (isNaN(userId)) {
        return res.status(400).json({ error: 'Invalid user ID' });
    }

    let sql = "SELECT * FROM register WHERE id = ?";
    conn.query(sql, [userId], function(err, result) {
        if (err) {
            console.error('Error querying database: ', err);
            res.status(500).json({ error: 'Internal server error' });
            return;
        }

        if (result.length > 0) {
            res.json({
                status: 200,
                error: null,
                response: {
                    events: result.map(register => ({
                        username: register.username,
                        nama: register.nama,
                        email: register.email,
                        telepon: register.telepon    
                    }))
                }
            });
        } else {
            res.status(404).json({ error: 'User not found' });
        }
    });
});


//update data user dengan id di mobile client
app.put('/api/pat/register/:id', function(req, res) {
    let id = parseInt(req.params.id);
    if (isNaN(id)) {
        return res.status(400).json({ error: 'Invalid user ID' });
    }

    let newData = {
        username: req.body.username,
        pass: req.body.pass,
        nama: req.body.nama,
        email: req.body.email,
        telepon: req.body.telepon
    };

    let sql = "UPDATE register SET ";
    let values = [];

    // Check if each field is present in the request body
    if (newData.username) {
        sql += "username = ?, ";
        values.push(newData.username);
    }
    if (newData.pass) {
        sql += "pass = ?, ";
        values.push(newData.pass);
    }
    if (newData.nama) {
        sql += "nama = ?, ";
        values.push(newData.nama);
    }
    if (newData.email) {
        sql += "email = ?, ";
        values.push(newData.email);
    }
    if (newData.telepon) {
        sql += "telepon = ?, ";
        values.push(newData.telepon);
    }

    // Remove the trailing comma and space
    sql = sql.slice(0, -2);

    // Add the WHERE clause
    sql += " WHERE id = ?";
    values.push(id);

    conn.query(sql, values, function(err, result) {
        if (err) {
            console.error('Error updating data: ', err);
            res.status(500).json({ error: 'Internal server error' });
            return;
        }
        if (result.affectedRows == 0) {
            // No rows were affected, meaning the user doesn't exist
            res.status(404).json({ error: 'User not found' });
            return;
        }
        res.json({
            status: 200,
            error: null,
            response: "Data updated successfully"
        });
    });
});


//membuat event
app.post('/api/pat/event', function(req, res) {
    try {
        // Parse body data
        let nama_konser = req.body.nama_konser;
        let artis = req.body.artis || ''; // Set default value if not provided
        let deskripsi = req.body.deskripsi;
        let kategoriTiket = req.body.kategori_tiket;
        let lokasi = req.body.lokasi;
        let tanggal = req.body.tanggal;
        let metodePembayaran = req.body.metode_pembayaran;

        // Check if kategoriTiket and metodePembayaran are strings and parse them if needed
        if (typeof kategoriTiket === 'string') {
            kategoriTiket = JSON.parse(kategoriTiket);
        }
        if (typeof metodePembayaran === 'string') {
            metodePembayaran = JSON.parse(metodePembayaran);
        }

        // Calculate total tickets
        let jumlahTiket = kategoriTiket.reduce((total, kategori) => total + (kategori.jumlah || 0), 0);

        // Create data object
        let data = {
            nama_konser,
            artis,
            deskripsi,
            kategori_tiket: JSON.stringify(kategoriTiket), // Convert array to JSON string
            jumlah_tiket: jumlahTiket, // Use calculated total tickets
            lokasi,
            tanggal,
            metode_pembayaran: JSON.stringify(metodePembayaran) // Convert array to JSON string
        };

        // Insert data into database
        let sql = "INSERT INTO event SET ?";
        conn.query(sql, data, function(err, result) {
            if (err) {
                console.error('Error inserting event: ', err);
                res.status(500).json({ error: 'Internal server error' });
                return;
            }
            res.json({
                "status": 200,
                "error": null,
                "response": "Event created successfully",
                "event_id": result.insertId
            });
        });
    } catch (error) {
        console.error('Error processing request: ', error);
        res.status(400).json({ error: 'Invalid request data' });
    }
});


//update event berdasarkan id
app.put('/api/pat/event/desktop/:id', upload.single('gambar'), function(req, res) {
    let id = req.params.id;
    let newData = {
        nama_konser: req.body.nama_konser,
        artis: req.body.artis, 
        deskripsi: req.body.deskripsi,
        kategori_tiket: req.body.kategori_tiket ? JSON.stringify(req.body.kategori_tiket) : null, // Convert to JSON string if present
        jumlah_tiket: req.body.jumlah_tiket,
        lokasi: req.body.lokasi,
        tanggal: req.body.tanggal,
        metode_pembayaran: req.body.metode_pembayaran ? JSON.stringify(req.body.metode_pembayaran) : null, // Convert to JSON string if present
        
    };

    let sql = "UPDATE event SET ";
    let values = [];

    // Check if each field is present in the request body
    if (newData.nama_konser) {
        sql += "nama_konser = ?, ";
        values.push(newData.nama_konser);
    }
    if (newData.artis) {
        sql += "artis = ?, ";
        values.push(newData.artis);
    }
    if (newData.deskripsi) {
        sql += "deskripsi = ?, ";
        values.push(newData.deskripsi);
    }
    if (newData.kategori_tiket) {
        sql += "kategori_tiket = ?, ";
        values.push(newData.kategori_tiket);
    }
    if (newData.jumlah_tiket) {
        sql += "jumlah_tiket = ?, ";
        values.push(newData.jumlah_tiket);
    }
    if (newData.lokasi) {
        sql += "lokasi = ?, ";
        values.push(newData.lokasi);
    }
    if (newData.tanggal) {
        sql += "tanggal = ?, ";
        values.push(newData.tanggal);
    }
    if (newData.metode_pembayaran) {
        sql += "metode_pembayaran = ?, ";
        values.push(newData.metode_pembayaran);
    }

    // Remove the trailing comma and space
    sql = sql.slice(0, -2);

    // Add the WHERE clause
    sql += " WHERE id = ?";
    values.push(id);

    conn.query(sql, values, function(err, result) {
        if (err) {
            console.error('Error updating event: ', err);
            res.status(500).json({ error: 'Internal server error' });
            return;
        }
        if (result.affectedRows == 0) {
            // No rows were affected, meaning the event doesn't exist
            res.status(404).json({ error: 'Event not found' });
            return;
        }
        res.json({
            "status": 200,
            "error": null,
            "response": "Event updated successfully"
        });
    });
});


//delete event berdasarkan id
app.delete('/api/pat/event/desktop/:id', function(req, res) {
    let id = req.params.id;
    
    let sql = "DELETE FROM event WHERE id = ?";
    conn.query(sql, [id], function(err, result) {
        if (err) {
            console.error('Error deleting event: ', err);
            res.status(500).json({ error: 'Internal server error' });
            return;
        }
        if (result.affectedRows == 0) {
            // No rows were affected, meaning the event doesn't exist
            res.status(404).json({ error: 'Event not found' });
            return;
        }
        res.json({
            "status": 200,
            "error": null,
            "response": "Event deleted successfully"
        });
    });
});


// menampilkan data event di desktop client
app.get('/api/pat/event/desktop', function(req, res) {
    let sql = "SELECT * FROM event";
    conn.query(sql, function(err, result) {
        if (err) {
            console.error('Error fetching events: ', err);
            res.status(500).json({ error: 'Internal server error' });
            return;
        }

        res.json({
            "status": 200,
            "error": null,
            "response": {
                events: result.map(event => {
                    return {
                        id: event.id,
                        nama_konser: event.nama_konser,
                        artist: event.artis,
                        deskripsi: event.deskripsi, 
                        kategori_tiket: JSON.parse(event.kategori_tiket), // Convert JSON string back to object
                        jumlah_tiket: event.jumlah_tiket,
                        lokasi: event.lokasi,
                        tanggal: event.tanggal,
                        metode_pembayaran: JSON.parse(event.metode_pembayaran), // Convert JSON string back to object
                    };
                })
            }
        });
    });
});


// menampilkan data event di mobile client
app.get('/api/pat/event/mobile', function(req, res) {
    let sql = "SELECT * FROM event";
    conn.query(sql, function(err, result) {
        if (err) {
            console.error('Error fetching events: ', err);
            res.status(500).json({ error: 'Internal server error' });
            return;
        }

        res.json({
            "status": 200,
            "error": null,
            "response": {
                events: result.map(event => {
                    // Parse the kategori_tiket JSON string
                    const kategori_tiket = JSON.parse(event.kategori_tiket);

                    // Extract the lowest price from kategori_tiket
                    const harga_terkecil = Math.min(...kategori_tiket.map(ticket => ticket.harga));

                    return {
                        id: event.id, 
                        nama_konser: event.nama_konser,
                        artist: event.artis,
                        harga: harga_terkecil,
                        lokasi: event.lokasi,
                        tanggal: event.tanggal

                    };
                })
            }
        });
    });
});


//menampilkan data event yang ingin dipesan di mobile client
app.get('/api/pat/event/mobile/:id', function(req, res) {
    let sql = "SELECT * FROM event WHERE id = ?";
    conn.query(sql, [req.params.id], function(err, result) {
        if (err) {
            console.error('Error fetching events: ', err);
            res.status(500).json({ error: 'Internal server error' });
            return;
        }

        if (result.length === 0) {
            res.status(404).json({ error: 'Event not found' });
            return;
        }

        const event = result[0];
        const kategori_tiket = JSON.parse(event.kategori_tiket);
        const kategori_order = kategori_tiket.map(ticket => ticket.kategori);

        res.json({
            "status": 200,
            "error": null,
            "response": {
                events: [{
                    kategori: kategori_order
                }]
            }
        });
    });
});


//membuat order
app.post('/api/pat/order', function(req, res) {
    const { eventId, kategori_tiket } = req.body;

    if (!Array.isArray(kategori_tiket) || kategori_tiket.length === 0) {
        return res.status(400).json({ error: 'Invalid kategori_tiket format' });
    }

    const sql = "SELECT * FROM event WHERE id = ?";

    conn.query(sql, [eventId], function(err, result) {
        if (err) {
            console.error('Error retrieving event: ', err);
            return res.status(500).json({ error: 'Internal server error' });
        }

        if (result.length === 0) {
            return res.status(404).json({ error: 'Event not found' });
        }

        const event = result[0];

        let kategori_tiket_parsed;
        try {
            kategori_tiket_parsed = JSON.parse(event.kategori_tiket);
        } catch (error) {
            console.error('Error parsing JSON: ', error);
            return res.status(500).json({ error: 'Internal server error' });
        }

        let total_harga = 0;
        let total_jumlah_tiket = 0;

        for (const tiket of kategori_tiket) {
            const { kategori, jumlah } = tiket;
            if (isNaN(jumlah) || jumlah < 1) {
                return res.status(400).json({ error: 'Invalid jumlah for a ticket category' });
            }

            const ticketCategory = kategori_tiket_parsed.find(category => category.kategori === kategori);
            if (!ticketCategory) {
                return res.status(400).json({ error: `Invalid ticket category: ${kategori}` });
            }

            const harga_tiket = ticketCategory.harga;
            if (isNaN(harga_tiket)) {
                console.error('Invalid ticket price:', harga_tiket);
                return res.status(500).json({ error: 'Internal server error' });
            }

            total_harga += harga_tiket * jumlah;
            total_jumlah_tiket += jumlah;

            if (ticketCategory.jumlah < jumlah) {
                return res.status(400).json({ error: `Not enough tickets available for category: ${kategori}` });
            }

            // Reduce the available tickets for the category
            ticketCategory.jumlah -= jumlah;
        }

        const updatedJumlahTiket = event.jumlah_tiket - total_jumlah_tiket;

        const orderData = {
            eventId,
            total_harga,
            kategori_tiket: JSON.stringify(kategori_tiket) // Store as JSON string
        };

        const updateEventSql = "UPDATE event SET kategori_tiket = ?, jumlah_tiket = ? WHERE id = ?";
        const updatedKategoriTiket = JSON.stringify(kategori_tiket_parsed);

        conn.query(updateEventSql, [updatedKategoriTiket, updatedJumlahTiket, eventId], function(err, result) {
            if (err) {
                console.error('Error updating event: ', err);
                return res.status(500).json({ error: 'Internal server error' });
            }

            const orderSql = "INSERT INTO `order` (`eventId`, `kategori_tiket`, `total_harga`) VALUES (?, ?, ?)";
            conn.query(orderSql, [orderData.eventId, orderData.kategori_tiket, orderData.total_harga], function(err, result) {
                if (err) {
                    console.error('Error inserting order: ', err);
                    return res.status(500).json({ error: 'Internal server error' });
                }

                res.json({
                    status: 200,
                    error: null,
                    response: "Order created successfully",
                    order: {
                        eventId: orderData.eventId,
                        kategori_tiket: orderData.kategori_tiket,
                        total_harga: orderData.total_harga
                    },
                    order_id: result.insertId
                });
            });
        });
    });
});


//menampilkan data order di desktop
app.get('/api/pat/order', function(req, res) {
    const sql = "SELECT * FROM `order`";

    conn.query(sql, function(err, result) {
        if (err) {
            console.error('Error retrieving orders: ', err);
            res.status(500).json({ error: 'Internal server error' });
            return;
        }

        res.json({
            "status": 200,
            "error": null,
            "response": result
        });
    });
});


//menampilkan data order di mobile client berdasarkan id
app.get('/api/pat/order/:id', function(req, res) {
    const orderId = parseInt(req.params.id);
    if (!orderId) {
        return res.status(400).json({ error: 'Missing order ID' });
    }

    const orderSql = "SELECT eventId, total_harga FROM `order` WHERE id = ?";
    conn.query(orderSql, [orderId], function(err, orderResult) {
        if (err) {
            console.error('Error retrieving order: ', err);
            return res.status(500).json({ error: 'Internal server error' });
        }

        if (orderResult.length === 0) {
            return res.status(404).json({ error: 'Order not found' });
        }

        const eventId = orderResult[0].eventId;
        const total_harga = orderResult[0].total_harga;

        const eventSql = "SELECT metode_pembayaran FROM event WHERE id = ?";
        conn.query(eventSql, [eventId], function(err, eventResult) {
            if (err) {
                console.error('Error retrieving event: ', err);
                return res.status(500).json({ error: 'Internal server error' });
            }

            if (eventResult.length === 0) {
                return res.status(404).json({ error: 'Event not found' });
            }

            let metode_pembayaran_parsed;
            try {
                metode_pembayaran_parsed = JSON.parse(eventResult[0].metode_pembayaran);
            } catch (error) {
                console.error('Error parsing metode_pembayaran JSON: ', error);
                return res.status(500).json({ error: 'Internal server error' });
            }

            const banks = metode_pembayaran_parsed.map(method => method.bank);

            res.json({
                "status": 200,
                "error": null,
                "response": {
                    "total_harga": total_harga,
                    "bank": banks
                }
            });
        });
    });
});

//membuat payment
app.post('/api/pat/payment', function(req, res) {
    const { orderId, metode_pembayaran } = req.body;

    if (!orderId) {
        return res.status(400).json({ error: 'Missing orderId' });
    }

    const orderSql = "SELECT eventId FROM `order` WHERE id = ?";
    conn.query(orderSql, [orderId], function(err, orderResult) {
        if (err) {
            console.error('Error retrieving order: ', err);
            return res.status(500).json({ error: 'Internal server error' });
        }

        if (orderResult.length === 0) {
            return res.status(404).json({ error: 'Order not found' });
        }

        const eventId = orderResult[0].eventId;

        const eventSql = "SELECT metode_pembayaran FROM event WHERE id = ?";
        conn.query(eventSql, [eventId], function(err, eventResult) {
            if (err) {
                console.error('Error retrieving event: ', err);
                return res.status(500).json({ error: 'Internal server error' });
            }

            if (eventResult.length === 0) {
                return res.status(404).json({ error: 'Event not found' });
            }

            let metode_pembayaran_parsed;
            try {
                metode_pembayaran_parsed = JSON.parse(eventResult[0].metode_pembayaran);
            } catch (error) {
                console.error('Error parsing metode_pembayaran JSON: ', error);
                return res.status(500).json({ error: 'Internal server error' });
            }

            const paymentMethod = metode_pembayaran_parsed.find(method => method.bank === metode_pembayaran);
            if (!paymentMethod) {
                return res.status(400).json({ error: 'Invalid payment method' });
            }

            const kode = paymentMethod.kode;

            // Generate 12 random digits for the virtual account
            const randomDigits = () => Math.floor(Math.random() * 10);
            const virtual_account = `${kode}${Array.from({ length: 12 }, randomDigits).join('')}`;
            const status = "Menunggu pembayaran";

            const paymentData = {
                orderId,
                metode_pembayaran,
                virtual_account,
                status
            };

            const paymentSql = "INSERT INTO payment SET ?";
            conn.query(paymentSql, paymentData, function(err, result) {
                if (err) {
                    console.error('Error inserting payment: ', err);
                    return res.status(500).json({ error: 'Internal server error' });
                }

                res.json({
                    status: 200,
                    error: null,
                    response: "Payment created successfully",
                    payment: {
                        orderId: paymentData.orderId,
                        metode_pembayaran: paymentData.metode_pembayaran,
                        virtual_account: paymentData.virtual_account,
                        status: paymentData.status
                    },
                    payment_id: result.insertId
                });
            });
        });
    });
});


//menampilkan data payment di mobile client
app.get('/api/pat/payment/:id', function(req, res) {
    const paymentId = req.params.id;

    const paymentSql = "SELECT * FROM payment WHERE id = ?";

    conn.query(paymentSql, [paymentId], function(err, result) {
        if (err) {
            console.error('Error retrieving payment: ', err);
            res.status(500).json({ error: 'Internal server error' });
            return;
        }

        if (result.length === 0) {
            res.status(404).json({ error: 'Payment not found' });
            return;
        }

        res.json({
            "status": 200,
            "error": null,
            "response": {
                events: result.map(payment => {
                    return {
                        id: payment.id,
                        orderId: payment.orderId,
                        metode_pembayaran: payment.metode_pembayaran,
                        virtual_account: payment.virtual_account,
                        status: payment.status    
                    };
                })
            }
        });
    });
});


//menampilkan data payment di desktop
app.get('/api/pat/payment', function(req, res) {
    const sql = "SELECT * FROM payment";

    conn.query(sql, function(err, result) {
        if (err) {
            console.error('Error retrieving payments: ', err);
            res.status(500).json({ error: 'Internal server error' });
            return;
        }

        res.json({
            "status": 200,
            "error": null,
            "response": result
        });
    });
});


function generateKodeBooking() {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
    let kodeBooking = '';
    for (let i = 0; i < 7; i++) {
        kodeBooking += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    return kodeBooking;
}

//confirm
app.post('/api/pat/confirm', function(req, res) {
    console.log('Request body:', req.body); // Logging request body
    const { orderId } = req.body;

    // Pastikan orderId ada dalam request body
    if (!orderId) {
        console.log('Missing orderId'); // Logging error
        return res.status(400).json({ error: 'orderId is required' });
    }

    // Tentukan status dan kode_booking
    const status = "Pembayaran berhasil";
    const kode_booking = generateKodeBooking();

    // Buat objek data yang akan disimpan ke database
    const data = {
        orderId,
        status,
        kode_booking,
    };

    // Query untuk menyimpan data ke database
    const sql = "INSERT INTO confirm SET ?";
    conn.query(sql, data, function(err, result) {
        if (err) {
            console.error('Error inserting confirm: ', err);
            return res.status(500).json({ error: 'Internal server error' });
        }
        res.json({
            orderId,
            status,
            kode_booking,
        });
    });
});


//menampilkan data confirm di mobile client berdasarkan id
app.get('/api/pat/confirm/:orderId', function(req, res) {
    const orderId = parseInt(req.params.orderId);
    if (!orderId) {
        return res.status(400).json({ error: 'Missing order ID' });
    }

    const sql = "SELECT orderId, status, kode_booking FROM confirm WHERE orderId = ?";
    conn.query(sql, [orderId], function(err, result) {
        if (err) {
            console.error('Error fetching order: ', err);
            return res.status(500).json({ error: 'Internal server error' });
        }

        if (result.length === 0) {
            return res.status(404).json({ error: 'Order not found' });
        }

        res.json(result[0]);
    });
});


//menampilkan data confirm di desktop
app.get('/api/pat/confirm', function(req, res) {
    const sql = "SELECT * FROM confirm";

    conn.query(sql, function(err, result) {
        if (err) {
            console.error('Error retrieving confirm: ', err);
            res.status(500).json({ error: 'Internal server error' });
            return;
        }

        res.json({
            "status": 200,
            "error": null,
            "response": result
        });
    });
});


//buat server http yang akan mendengar permintaan pada port 8000
var server = app.listen(8000, function () {
    //menampilkan pesan bahwa server API sedang berjalan pada port 8000
    console.log("API Server running at port 8000");
});
