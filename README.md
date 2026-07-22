# 🚗 TVDE Tracker

Aplicativo Android desenvolvido para motoristas TVDE (transporte por aplicativo) acompanharem suas jornadas de trabalho, registrando sessões, trajetos percorridos e informações relacionadas à atividade diária.

A proposta do projeto é criar uma experiência semelhante ao Strava, porém adaptada para profissionais que trabalham dirigindo por aplicativos, permitindo acompanhar deslocamentos, tempo de trabalho e histórico de rotas.

---

## 📱 Sobre o projeto

Motoristas TVDE geralmente precisam controlar manualmente informações como tempo trabalhado, deslocamentos realizados e distância percorrida durante suas jornadas.

O **TVDE Tracker** foi criado para centralizar esses dados em um aplicativo Android, permitindo:

- iniciar e finalizar sessões de trabalho;
- registrar trajetos realizados;
- visualizar histórico de jornadas;
- acompanhar rotas através do mapa;
- armazenar informações localmente para consulta posterior.

O projeto também tem como objetivo explorar conceitos de desenvolvimento Android moderno, incluindo arquitetura MVVM, persistência local, gerenciamento de estado e recursos de localização.

---

# ✨ Funcionalidades

## 🕒 Gerenciamento de sessões

- Início e encerramento de jornadas de trabalho;
- Registro de horário de início e fim;
- Associação dos dados de localização a cada sessão;
- Histórico das sessões realizadas.

## 📍 Rastreamento e localização

- Registro de pontos de localização durante a jornada;
- Armazenamento de latitude, longitude e timestamp;
- Associação entre pontos GPS e sessões de trabalho;
- Estrutura preparada para acompanhamento contínuo de deslocamento.

## 🗺️ Visualização de rotas

- Exibição dos trajetos realizados no mapa;
- Consulta visual dos deslocamentos;
- Reconstrução das rotas através dos pontos registrados.

## 📊 Informações da jornada

- Visualização dos dados da sessão;
- Organização das informações de trabalho;
- Base preparada para futuras métricas de produtividade.

---

# 🚧 Status do projeto

**Em desenvolvimento — versão funcional em estágio avançado.**

As principais funcionalidades do aplicativo já estão implementadas. As próximas etapas são voltadas para melhorias de experiência, otimizações e preparação para uma versão de lançamento.

---

# 🛠️ Tecnologias utilizadas

## Android

- **Kotlin**
  - Linguagem principal do projeto

- **Jetpack Compose**
  - Construção da interface utilizando UI declarativa moderna

- **Room Database**
  - Persistência local dos dados

- **KSP (Kotlin Symbol Processing)**
  - Geração de código para integração com bibliotecas Android

- **MVVM**
  - Arquitetura utilizada para organização e separação de responsabilidades

- **Android Location APIs**
  - Recursos relacionados ao gerenciamento de localização

---

# 🏗️ Arquitetura

O projeto utiliza uma arquitetura baseada em MVVM:

```
UI (Jetpack Compose)
        |
        ↓
ViewModel
        |
        ↓
Repository
        |
        ↓
Room Database
        |
        ↓
SQLite
```

A separação das responsabilidades facilita:

- manutenção do código;
- evolução das funcionalidades;
- testes;
- organização da regra de negócio.

---

# 🗄️ Modelo de dados

O aplicativo possui duas entidades principais:

## Sessao

Representa uma jornada de trabalho do motorista.

Principais campos:

```
id
horaInicio
horaFim
distanciaTotalMetros
```

---

## PontoGps

Representa um ponto de localização registrado durante uma sessão.

Principais campos:

```
id
latitude
longitude
timestamp
sessaoId
```

Cada sessão possui uma relação com seus pontos GPS, permitindo armazenar e reconstruir os trajetos realizados.

---

# 🚀 Como executar o projeto

Clone o repositório:

```bash
git clone https://github.com/ranielschneider/TvdeTracker.git
```

Abra o projeto no Android Studio.

Aguarde a sincronização das dependências e execute em um dispositivo físico ou emulador.

Requisitos:

- Android SDK 24+
- Android Studio atualizado

---

# 🎯 Objetivos técnicos

Este projeto foi desenvolvido para aprofundar conhecimentos em:

- Desenvolvimento Android moderno;
- Kotlin;
- Jetpack Compose;
- Arquitetura MVVM;
- Persistência local com Room;
- Modelagem de dados;
- Integração com recursos de localização;
- Construção de aplicações completas.

Além do aprendizado técnico, o projeto busca solucionar um problema real enfrentado por profissionais que trabalham com transporte por aplicativo.

---

# 👤 Autor

**Raniel Schneider**

GitHub:
https://github.com/ranielschneider

LinkedIn:
https://linkedin.com/in/raniel-schneider-79006b50
