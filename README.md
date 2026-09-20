# Fake GPS Route Simulator (Android/Kotlin)

Projeto de **localização simulada para testes de apps próprios**, com serviço foreground e atualização contínua em segundo plano.

> Não intercepta o Google Maps, não lê destinos digitados em outros aplicativos e não tenta ocultar a localização simulada. O destino é informado neste app como `latitude,longitude`.

## Como abrir

1. Abra no Android Studio Hedgehog ou mais recente.
2. Configure o Android SDK 35 e JDK 17.
3. No `local.properties` ou na configuração do Gradle, defina `MAPS_API_KEY` se for adicionar um mapa Google Maps. A tela inicial atual não precisa de uma chave.
4. Execute em um emulador ou aparelho de testes.
5. Em **Opções do desenvolvedor > Aplicativo de local fictício**, selecione `Route Simulator`.
6. Conceda a permissão de localização e informe, por exemplo, `-23.5505,-46.6333`.

## Limitações importantes

- `setMockLocation` exige que o app esteja selecionado como provedor de local fictício.
- Android pode exigir a permissão de localização em segundo plano dependendo do fluxo e da versão do sistema.
- O serviço fica visível como notificação foreground e pode ser parado pelo usuário.
- Para rotas reais com ruas, integre uma API de rotas autorizada (Google Routes/Directions ou outra) e substitua a interpolação simples por uma lista de pontos da rota. Não é possível controlar automaticamente qualquer destino digitado no Google Maps por uma API pública.

## Uso responsável

Use somente em aparelhos, emuladores e aplicativos sob seu controle, para QA, desenvolvimento e demonstrações. Não use para burlar regras de jogos, serviços, sistemas antifraude ou rastreamento.
