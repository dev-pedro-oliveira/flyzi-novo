# Flyzi - Busca de Passagens Aéreas

Projeto completo de busca de passagens aéreas com Landing Page minimalista e página de resultados.

## 🏗️ Estrutura do Projeto

```
flyzi-novo/
├── backend/          # Spring Boot + H2
│   ├── pom.xml
│   └── src/
│       └── main/java/com/flyzi/backend/
│           ├── FlyziBackendApplication.java
│           ├── model/Voo.java
│           ├── controller/VooController.java
│           ├── repository/VooRepository.java
│           └── config/DataSeeder.java
│       └── resources/
│           └── application.properties
│
└── frontend/         # HTML/CSS/JS
    ├── index.html
    ├── resultados.html
    ├── style-landing.css
    ├── style.css
    ├── app-landing.js
    └── app.js
```

## 🚀 Como Rodar

### Backend

```bash
cd backend
./mvnw clean install
./mvnw spring-boot:run
```

Backend rodará em: `http://localhost:8080`

### Frontend

Abra no navegador:
```
frontend/index.html
```

## 📋 Features

### Landing Page
- ✅ Design minimalista com cores claras
- ✅ Checkbox Ida/Ida e Volta
- ✅ Campos De, Para (apenas cidades)
- ✅ Seletor de passageiros (1-3)
- ✅ Calendário customizado (Ida e Volta)
- ✅ Validação completa

### Página de Resultados
- ✅ Listagem de voos com design limpo
- ✅ Filtro por companhia
- ✅ Filtro apenas voos diretos
- ✅ Layout responsivo
- ✅ Informações detalhadas de cada voo

## 🎨 Paleta de Cores

- **Background**: #fafbfc (Cinza muito claro)
- **Cards**: #ffffff (Branco)
- **Texto**: #2c3e50 (Cinza escuro)
- **Secundário**: #7f8c8d (Cinza médio)
- **Destaque**: #4a90e2 (Azul)
- **Borda**: #e0e6ed (Cinza leve)

## 📱 Responsividade

Totalmente responsivo para dispositivos móveis.

## 🗄️ Banco de Dados

Usa H2 em memória para testes rápidos. Dados são gerados automaticamente ao iniciar a aplicação.

- 625 voos mockados
- 5 origens + 5 destinos
- Dados aleatórios realistas

## 📝 API

### GET /api/voos
Retorna todos os voos cadastrados.

**Resposta:**
```json
[
  {
    "id": 1,
    "origem": "GRU",
    "destino": "LIS",
    "descricaoRota": "GRU para LIS",
    "data": "01 de junho de 2026",
    "dataISO": "2026-06-01",
    "horario": "10:30 - 22:45",
    "duracaoTexto": "10h 45m",
    "duracaoMinutos": 645,
    "tipo": "Direto",
    "preco": 850.50,
    "companhia": "Azul",
    "classeCor": "tag-azul",
    "milhasNum": 15309,
    "milhasFormatado": "15.309",
    "teveQueda": false,
    "continente": "Europa",
    "categoria": "Geral"
  }
]
```

## 🛠️ Tecnologias

- **Backend**: Java 21, Spring Boot 3.2, JPA, H2
- **Frontend**: HTML5, CSS3, JavaScript ES6+
- **Build**: Maven

## 📄 Licença

MIT
