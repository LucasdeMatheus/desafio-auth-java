
## 1. Visão Geral da Arquitetura

1. **Servidor Backend (API)**  
   - Pode ser implementado em Node.js (Express, NestJS), Python (Flask, Django), Java (Spring Boot) etc.
   - Comunicação via **REST** (JSON).

2. **Banco de Dados**  
   - Banco relacional (MySQL, PostgreSQL) ou NoSQL (MongoDB, Firestore).
   - Tabelas/Coleções primárias: 
     - `users` (armazenando dados do usuário, inclusive colunas/campos para login social, se necessário).
   
3. **Serviço de Email**  
   - Uso de biblioteca como **Nodemailer** (Node.js) ou **smtplib** (Python).
   - Necessário configurar um provedor de email (Gmail, SendGrid, etc.).

4. **Autenticação e Autorização**  
   - JWT (JSON Web Token) para autenticação tradicional (email/senha).
   - OAuth 2.0 para login com Google.

5. **Bibliotecas/SDK**  
   - Google API Client para obter dados via OAuth 2.0.
   - Ou bibliotecas como **Passport** (Node.js) / **Social Auth** (Python Django) para lidar com login social.

---

## 2. Endpoints Principais

### 2.1. Cadastro de Usuário (email/senha)

- **Endpoint**: `POST /auth/register`
- **Corpo da requisição (JSON)**:
  ```json
  {
    "name": "Fulano de Tal",
    "email": "fulano@example.com",
    "password": "123456"
  }
  ```
- **Validações**:
  - `email` deve ser único.
  - `password` deve atender critérios mínimos (tamanho mínimo, caracteres especiais etc.).
- **Retorno**:
  - Sucesso:
    ```json
    {
      "message": "Usuário criado com sucesso",
      "user": {
        "id": "...",
        "name": "...",
        "email": "...",
        ...
      }
    }
    ```
  - Erro (ex: email duplicado, campos inválidos):
    ```json
    {
      "error": "Email já está em uso"
    }
    ```
  
### 2.2. Login Tradicional (email/senha)

- **Endpoint**: `POST /auth/login`
- **Corpo da requisição (JSON)**:
  ```json
  {
    "email": "fulano@example.com",
    "password": "123456"
  }
  ```
- **Lógica**:
  1. Verificar se usuário existe no banco.
  2. Comparar a senha informada com a senha armazenada (hash + salt se necessário).
  3. Gerar e retornar um **JWT** contendo `userId`, `email` etc.
- **Retorno**:
  - Sucesso:
    ```json
    {
      "message": "Login realizado com sucesso",
      "token": "jwt_aqui",
      "user": {
        "id": "...",
        "email": "...",
        ...
      }
    }
    ```
  - Erro (email não encontrado, senha incorreta etc.):
    ```json
    {
      "error": "Credenciais inválidas"
    }
    ```

### 2.3. Login com Google (OAuth 2.0)

Há várias abordagens para o login com Google. Uma das mais comuns em Node.js é via **Passport** com o `passport-google-oauth20`. No entanto, conceitualmente, segue o fluxo:

1. **Endpoint**: `GET /auth/google`  
   - Redireciona o usuário para a página de login do Google (caso seja uma aplicação web).  
   - Em aplicações mobile, você pode usar bibliotecas que abrem o navegador no endpoint do Google e retornam o **token** para seu backend.

2. **Endpoint**: `GET /auth/google/callback`  
   - Recebe o **código de autorização** do Google.  
   - No backend, troca o código pelo **access token** e pelos **dados do perfil** do usuário (nome, email, foto, etc.).

3. **Lógica Interna**:
   - A partir do email retornado pelo Google, verificar se o usuário já existe no banco de dados.  
     - Se não existir, criar registro (com `googleId`, `email`, `name`, etc.).  
     - Se existir, apenas atualizar dados ou prosseguir com o login.
   - Gerar e retornar **JWT** para que a aplicação cliente continue autenticada.

4. **Resposta**:
   - Pode ser um redirecionamento para sua aplicação frontend ou um JSON com o token e dados do usuário.

Para **APIs** que rodam separadas do frontend, você pode expor um endpoint como `POST /auth/google/login` onde o **access token** do Google (obtido no frontend) é enviado. Então, no backend:
- Valida o token com as chaves públicas do Google.
- Extrai o email do payload.
- Cria ou atualiza o usuário.
- Gera um **JWT** próprio e responde.

### 2.4. Recuperação de Senha (envio de email)

- **Endpoint**: `POST /auth/forgot-password`
- **Corpo da requisição (JSON)**:
  ```json
  {
    "email": "fulano@example.com"
  }
  ```
- **Lógica**:
  1. Verificar se usuário existe no banco.
  2. Gerar um **token temporário** (pode ser um JWT de curta duração ou token aleatório salvo em banco).
  3. Enviar por email um link para redefinição de senha, por exemplo:
     ```
     https://seu-dominio.com/reset-password?token=TOKEN_AQUI
     ```
- **Retorno**:
  ```json
  {
    "message": "Se o email existir, foi enviado um link de recuperação"
  }
  ```

### 2.5. Redefinição de Senha (com token)

- **Endpoint**: `POST /auth/reset-password`
- **Corpo da requisição (JSON)**:
  ```json
  {
    "token": "TOKEN_RECEBIDO",
    "newPassword": "novaSenhaSegura123"
  }
  ```
- **Lógica**:
  1. Validar o token (se ainda está ativo, se corresponde ao usuário certo).
  2. Atualizar a senha do usuário (armazenando hash).
- **Retorno**:
  ```json
  {
    "message": "Senha atualizada com sucesso"
  }
  ```

---

## 3. Fluxo de Desenvolvimento (Exemplo em Node.js / Express)

Para ilustrar, segue um **roteiro** simplificado de implementação usando Node.js e Express:

1. **Configurar Projeto**  
   ```bash
   mkdir user-api && cd user-api
   npm init -y
   npm install express bcrypt jsonwebtoken nodemailer passport passport-google-oauth20 dotenv
   ```

2. **Estrutura de Pastas** (exemplo)
   ```
   user-api/
   ├─ src/
   │  ├─ controllers/
   │  ├─ routes/
   │  ├─ models/
   │  └─ config/
   ├─ .env
   └─ index.js
   ```

3. **index.js** – Iniciando servidor Express
   ```js
   require('dotenv').config();
   const express = require('express');
   const userRoutes = require('./src/routes/userRoutes');
   const authRoutes = require('./src/routes/authRoutes');

   const app = express();
   app.use(express.json());

   // Rotas
   app.use('/users', userRoutes);
   app.use('/auth', authRoutes);

   const PORT = process.env.PORT || 3000;
   app.listen(PORT, () => {
     console.log(`Servidor rodando na porta ${PORT}`);
   });
   ```

4. **Configurar Banco de Dados** (exemplo MongoDB)
   - Instale mongoose: `npm install mongoose`
   - `src/config/db.js`:
     ```js
     const mongoose = require('mongoose');

     const connectDB = async () => {
       try {
         await mongoose.connect(process.env.MONGO_URI, {
           useNewUrlParser: true,
           useUnifiedTopology: true
         });
         console.log('Conectado ao MongoDB');
       } catch (error) {
         console.error('Erro ao conectar ao MongoDB:', error);
       }
     };

     module.exports = connectDB;
     ```
   - Chame `connectDB()` em `index.js`.

5. **Criar Model de Usuário** – `src/models/User.js`
   ```js
   const mongoose = require('mongoose');

   const userSchema = new mongoose.Schema({
     name: { type: String, required: true },
     email: { type: String, required: true, unique: true },
     password: { type: String },
     googleId: { type: String }, // Para login via Google
     // outros campos, se necessário
   });

   module.exports = mongoose.model('User', userSchema);
   ```

6. **Criar Controllers** – Exemplo: `authController.js`

   ```js
   const bcrypt = require('bcrypt');
   const jwt = require('jsonwebtoken');
   const User = require('../models/User');

   // Cadastro
   exports.register = async (req, res) => {
     try {
       const { name, email, password } = req.body;

       // Verifica se email já existe
       const existingUser = await User.findOne({ email });
       if (existingUser) {
         return res.status(400).json({ error: 'Email já cadastrado' });
       }

       // Hash da senha
       const hashedPassword = await bcrypt.hash(password, 10);

       // Cria usuário
       const newUser = await User.create({
         name,
         email,
         password: hashedPassword
       });

       return res.status(201).json({
         message: 'Usuário criado com sucesso',
         user: {
           id: newUser._id,
           name: newUser.name,
           email: newUser.email
         }
       });
     } catch (error) {
       console.error(error);
       return res.status(500).json({ error: 'Erro interno do servidor' });
     }
   };

   // Login
   exports.login = async (req, res) => {
     try {
       const { email, password } = req.body;

       const user = await User.findOne({ email });
       if (!user) {
         return res.status(400).json({ error: 'Credenciais inválidas' });
       }

       const match = await bcrypt.compare(password, user.password);
       if (!match) {
         return res.status(400).json({ error: 'Credenciais inválidas' });
       }

       // Gera JWT
       const token = jwt.sign({ userId: user._id }, process.env.JWT_SECRET, {
         expiresIn: '1d',
       });

       return res.json({
         message: 'Login realizado com sucesso',
         token,
         user: {
           id: user._id,
           name: user.name,
           email: user.email
         }
       });
     } catch (error) {
       console.error(error);
       return res.status(500).json({ error: 'Erro interno do servidor' });
     }
   };
   ```

7. **Login com Google** – Exemplo usando `passport-google-oauth20`
   - **Config**: `src/config/passport.js`
     ```js
     const passport = require('passport');
     const GoogleStrategy = require('passport-google-oauth20').Strategy;
     const User = require('../models/User');

     passport.use(new GoogleStrategy({
       clientID: process.env.GOOGLE_CLIENT_ID,
       clientSecret: process.env.GOOGLE_CLIENT_SECRET,
       callbackURL: '/auth/google/callback'
     },
     async (accessToken, refreshToken, profile, done) => {
       try {
         // profile contém as infos do Google (id, emails, displayName, etc.)
         const existingUser = await User.findOne({ googleId: profile.id });

         if (existingUser) {
           return done(null, existingUser);
         }

         // Cria novo usuário se não existir
         const newUser = await User.create({
           googleId: profile.id,
           name: profile.displayName,
           email: profile.emails[0].value
         });

         return done(null, newUser);
       } catch (err) {
         return done(err, null);
       }
     }));
     ```

   - **Rotas**: `authRoutes.js`
     ```js
     const router = require('express').Router();
     const passport = require('passport');
     const authController = require('../controllers/authController');

     // Iniciar login com Google
     router.get('/google', 
       passport.authenticate('google', { scope: ['profile', 'email'] })
     );

     // Callback após login Google
     router.get('/google/callback', 
       passport.authenticate('google', { session: false }),
       (req, res) => {
         // Gerar nosso JWT
         const token = jwt.sign({ userId: req.user._id }, process.env.JWT_SECRET, {
           expiresIn: '1d',
         });
         return res.json({
           message: 'Login Google bem-sucedido',
           token,
           user: {
             id: req.user._id,
             name: req.user.name,
             email: req.user.email
           }
         });
       }
     );

     // Cadastro e login tradicionais
     router.post('/register', authController.register);
     router.post('/login', authController.login);

     module.exports = router;
     ```

8. **Envio de Email** (Recuperação de senha) – Exemplo com Nodemailer
   - `npm install nodemailer`
   - Em `authController.js`:
     ```js
     const nodemailer = require('nodemailer');
     
     exports.forgotPassword = async (req, res) => {
       try {
         const { email } = req.body;
         const user = await User.findOne({ email });
         if (!user) {
           return res.json({ message: 'Se o email existir, foi enviado um link de recuperação' });
         }

         // Gera token, por exemplo usando jwt
         const resetToken = jwt.sign({ userId: user._id }, process.env.JWT_SECRET, { expiresIn: '1h' });

         // Monta link
         const resetLink = `https://seu-dominio.com/reset-password?token=${resetToken}`;

         // Configura transporte
         const transporter = nodemailer.createTransport({
           service: 'Gmail',
           auth: {
             user: process.env.EMAIL_USER,
             pass: process.env.EMAIL_PASS
           }
         });

         // Monta email
         const mailOptions = {
           from: process.env.EMAIL_USER,
           to: user.email,
           subject: 'Recuperação de Senha',
           text: `Clique aqui para redefinir sua senha: ${resetLink}`
         };

         // Envia email
         await transporter.sendMail(mailOptions);

         return res.json({ message: 'Se o email existir, foi enviado um link de recuperação' });

       } catch (error) {
         console.error(error);
         return res.status(500).json({ error: 'Erro interno do servidor' });
       }
     };

     exports.resetPassword = async (req, res) => {
       try {
         const { token, newPassword } = req.body;
         const decoded = jwt.verify(token, process.env.JWT_SECRET);

         const user = await User.findById(decoded.userId);
         if (!user) {
           return res.status(400).json({ error: 'Token inválido' });
         }

         // Atualiza senha
         user.password = await bcrypt.hash(newPassword, 10);
         await user.save();

         return res.json({ message: 'Senha atualizada com sucesso' });
       } catch (error) {
         console.error(error);
         return res.status(400).json({ error: 'Token inválido ou expirado' });
       }
     };
     ```
   - **Rotas** (`authRoutes.js` ou outro arquivo):
     ```js
     router.post('/forgot-password', authController.forgotPassword);
     router.post('/reset-password', authController.resetPassword);
     ```

---

## 4. Considerações Finais

- **Segurança**:
  - Sempre use **HTTPS** em produção.
  - Armazene senhas usando hash seguro (ex: `bcrypt`).
  - Mantenha variáveis sensíveis no `.env`.
  - O token JWT não deve ser armazenado em localStorage de forma insegura no frontend.
  
- **Validações**:
  - Use bibliotecas (ex: `express-validator`) para validar entradas no backend.
  
- **Boas Práticas**:
  - Faça **logging** de erros de forma adequada (sem expor dados sensíveis).
  - Padronize respostas (códigos HTTP apropriados, corpo de resposta consistente).

Com esse **roteiro** (ou guia de implementação), você cobre o fluxo básico de **cadastro, login, login social (Google), envio de email e recuperação de senha**. Cada passo pode ser adaptado à linguagem e framework de sua preferência, mas o conceito e endpoints permanecem semelhantes.
