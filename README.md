# 🛡️ Auth API - Sistema de Autenticação com Suporte a OAuth2

API de autenticação para usuários e clientes, com suporte completo a:

- Login tradicional (e-mail/senha)
- Cadastro com verificação de e-mail
- Autenticação OAuth2 (`authorization_code`, `client_credentials`, `refresh_token`)
- Atualização de e-mail e senha
- Exclusão de usuário
- Tokens JWT (`access_token` e `refresh_token`)

---

## 🔑 Endpoints Principais

### 🔐 Login

```
POST /auth/login
```

**Body:**
```json
{
  "email": "usuario@email.com",
  "password": "senha123",
  "typeUser": "USER" // ou "CLIENT"
}
```

**Resposta:**
- `200 OK`: retorna ID, nome, email e token.
- `302 FOUND`: redireciona com `code` caso OAuth2 esteja ativo. Ex: http://localhost:5500/oauth-callback.html?code=595898
- `401 UNAUTHORIZED`: credenciais inválidas.

---

### 📝 Cadastro de Usuário/Cliente

```
POST /auth/register
```

**Body:**
```json
{
	"name": "exemplo",
    "email": "exemplo@gmail.com"
}
```

📨 Envia código de verificação por e-mail. Finalize com `/auth/verifyEmail`.

---

### ✅ Verificar Código do E-mail

```
POST /auth/verifyEmail
```

**Body:**
```json
{
  "code": "123456",
  "type": "VALIDEMAIL", // ou UPPASSWORD, UPEMAIL, DELETEUSER
  "typeUser": "USER",
  "userDTO": {
    "email": "lucas@email.com",
    "name": "Lucas"
  },
  "password": "senha123"
}
```

Realiza ações como:

- Confirmar cadastro (`VALIDEMAIL`)
- Trocar senha (`UPPASSWORD`)
- Atualizar e-mail (`UPEMAIL`)
- Excluir conta (`DELETEUSER`)

-- Obs: todos usam o mesmo endpoint, porem o method se comporta diferente, de acordo com o TYPE enviado no json.
---

### 🔁 Autorizar Aplicação (OAuth2 - Authorization Code)

```
GET /auth/authorize?client_id={clientId}&redirect_uri={uri}
```
- direciona para url .../auth/login(á ser desenvolvida)
- Valida clientId e redireciona para login.
- Após login, redireciona com url...`?code=XYZ123`.

---

### 🪙 Obter Token (OAuth2)

```
POST /auth/token
```

**Body (grant_type = authorization_code):**
```json
{
  "grantType": "AUTHORIZATION_CODE",
  "client_id": "client123",
  "client_secret": "segredo123",
  "code": "XYZ123"
}
```

**Body (grant_type = client_credentials):**
```json
{
  "grantType": "CLIENT_CREDENTIALS",
  "client_id": "client123",
  "client_secret": "segredo123"
}
```

**Body (grant_type = refresh_token):**
```json
{
  "grantType": "REFRESH_TOKEN",
  "client_id": "client123",
  "client_secret": "segredo123",
  "code": "XYZ123"
}
```
- todos retornam o tokenJWT de acordo com os scopes definidos.
---

## ⚙️ Configurações de Cliente

```
POST /client/config/id
```
```json
{
  "redirectUri": "http://localhost:5500/oauth-callback.html",
  "scopes": ["OPENID", "NAME"],
  "grantTypes": ["AUTHORIZATION_CODE", "REFRESH_TOKEN", "CLIENT_CREDENTIALS"]
}

```
- `redirectUri`: URI de redirecionamento após login.
- `scopes`: permissões associadas ao token.
- `grantTypes`: tipos de fluxo autorizados (`authorization_code`, `client_credentials`, `refresh_token`).
- o endpoint está protegida pelo JWT que irá comparar o ID
---

## 🧪 Exemplos de Fluxo OAuth2

### Fluxo de Autorização (authorization_code)

1. Usuário acessa:
```
GET /auth/authorize?client_id=abc&redirect_uri=https://app.com/callback
```

2. É redirecionado para o login(auth/login -> não configurada)
3. Após login:
```
https://app.com/callback?code=XYZ
```

4. Aplicação usa `/auth/token` para obter `access_token` e `refresh_token`

---

## 🛠️ Tecnologias

- Java 21
- Spring Boot
- Spring Security
- JWT (TokenService)
- OAuth2 personalizado (sem Spring Authorization Server)
- JPA/Hibernate
- Email com código de verificação

---
