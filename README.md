# TVDE Tracker
 
Aplicativo Android para motoristas TVDE registrarem suas sessões de trabalho e rotas percorridas, no estilo de um Strava voltado para o dia a dia de quem dirige por aplicativo.
 
## 📌 Status do projeto
 
🚧 **Em desenvolvimento** — a camada de persistência local (Room) já está funcional e validada. As próximas etapas são a captura de GPS em tempo real e o rastreamento em segundo plano.
 
- [x] Modelagem do banco de dados local (Room)
- [x] Entidades `Sessao` e `PontoGps` com relação de chave estrangeira
- [x] DAO com inserção e consulta de dados
- [x] Tela de teste validando escrita/leitura no banco
- [ ] Captura de localização em tempo real (GPS)
- [ ] Foreground Service para rastreamento contínuo
- [ ] Cálculo de distância percorrida por sessão
- [ ] Interface para iniciar/finalizar sessão de trabalho
- [ ] Histórico de sessões e visualização de rotas no mapa
## 🛠️ Tecnologias
 
- **Kotlin**
- **Jetpack Compose** — UI declarativa
- **Room** — persistência local
- **KSP** — geração de código do Room
- Arquitetura orientada a **MVVM** (em evolução conforme as próximas fases)
## 🗄️ Estrutura de dados
 
O app organiza os dados em duas entidades principais:
 
**`Sessao`** — representa um período de trabalho do motorista
- `id`, `horaInicio`, `horaFim`, `distanciaTotalMetros`
**`PontoGps`** — representa um ponto de localização capturado durante uma sessão
- `id`, `latitude`, `longitude`, `timestamp`, `sessaoId` (chave estrangeira, com exclusão em cascata)
## 🚀 Como rodar o projeto
 
1. Clone o repositório:
```bash
   git clone https://github.com/ranielschneider/TvdeTracker.git
```
2. Abra no Android Studio (versão mais recente recomendada).
3. Deixe o Gradle sincronizar as dependências.
4. Rode em um emulador ou dispositivo físico com **minSdk 24** ou superior.
Na tela inicial, o app cria uma sessão de teste, insere pontos de GPS fictícios no banco e exibe o resultado da consulta — validando que a camada Room está funcionando corretamente.
 
## 📖 Sobre o projeto
 
Este é um projeto pessoal desenvolvido para consolidar conhecimentos em **Kotlin**, **Jetpack Compose** e **arquitetura Android moderna**, ao mesmo tempo em que resolve um problema real da rotina como motorista TVDE: acompanhar rotas, tempo de trabalho e distância percorrida.
 
## 👤 Autor
 
**Raniel Schneider**
[GitHub](https://github.com/ranielschneider) · [LinkedIn](https://linkedin.com/in/raniel-schneider-79006b50)
