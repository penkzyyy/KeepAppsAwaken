Português / Portuguese



Keep Apps Awaken


App Android simples, sem anúncios e de código aberto, que mantém o ecrã ligado 

apenas enquanto as aplicações que escolheres estiverem em primeiro plano (por 

exemplo YouTube Music, YouTube ou Spotify).



Funcionalidades


- Lista de todas as aplicações instaladas, com pesquisa, para escolheres quais 

&#x20; mantêm o ecrã ligado
- Funciona em segundo plano de forma contínua (serviço em primeiro plano)
- Notificação silenciosa de importância mínima, que podes desativar nas 

&#x20; definições para ficar com a barra de estado limpa
- Arranque automático depois de reiniciar o telemóvel
- Pede isenção da otimização de bateria para não ser morta pelo sistema
- Sem permissão de internet, sem anúncios, sem rastreadores, sem serviço de 

&#x20; acessibilidade


Como funciona


A app usa o acesso às estatísticas de utilização do Android (UsageStatsManager) 

para saber qual a aplicação em primeiro plano. Quando é uma das que escolheste, 

mantém um wake lock que impede o ecrã de se apagar. Quando mudas de aplicação 

ou o ecrã é bloqueado, o wake lock é libertado.



Permissões


- Acesso a estatísticas de utilização: para saber qual a aplicação aberta
- Consultar aplicações instaladas: para mostrar a lista
- Serviço em primeiro plano: para correr continuamente
- Notificações: exigidas pelo Android para o serviço
- Ignorar otimização de bateria: para não ser terminada
- Arranque ao iniciar: para voltar a correr depois de reiniciar


Instalação


1. Instala o APK (permite instalar de fontes desconhecidas)
2. Abre a app e concede o acesso a estatísticas de utilização
3. Aceita a isenção de otimização de bateria
4. Seleciona as aplicações e liga o serviço
5. Para esconder o ícone na barra de estado: Definições > Aplicações > Keep Apps 

&#x20;  Awaken > Notificações > desativa o canal "Serviço ativo"


Compilar


Abre a pasta no Android Studio e usa Build > Build APK, ou na linha de comandos: 

./gradlew assembleRelease.



Créditos


Código desenvolvido por penkzyyy com a ajuda do Claude, da Anthropic.



Plano para a v2


- Ícone próprio da app
- Modo de ecrã: claro, escuro ou seguir o sistema


Licença


MIT. Vê o ficheiro LICENSE. Fica à vontade para editar, adaptar e redistribuir.







Inglês / English



Keep Apps Awaken



A simple, ad-free, open-source Android app that keeps the screen on only while the apps you’ve selected are in the foreground (for example, YouTube Music, YouTube, or Spotify).



Features



\- A list of all installed apps, with a search function, allowing you to choose which ones keep the screen on.

\- Runs continuously in the background (as a foreground service).

\- Includes a silent, low-priority notification, which can be disabled in the settings to keep your status bar clean.

\- Automatically starts up after the phone reboots.

\- Requests an exemption from battery optimization to prevent the system from terminating the app.

\- No internet permissions, no ads, no trackers, and no use of accessibility services.



How it works



The app uses Android’s usage statistics access (UsageStatsManager) to determine which application is currently in the foreground. When one of your selected apps is active, it maintains a "wake lock" that prevents the screen from turning off. When you switch to a different app or lock the screen, the wake lock is released. Permissions



\- Access usage statistics: to determine which application is currently open

\- Query installed applications: to display the list

\- Foreground service: to ensure continuous operation

\- Notifications: required by Android for the service

\- Ignore battery optimization: to prevent the app from being terminated

\- Run at startup: to resume operation after a device restart



Installation



1. Install the APK (ensure installation from unknown sources is enabled)

2\. Open the app and grant access to usage statistics

3\. Accept the battery optimization exemption

4\. Select the applications and enable the service

5\. To hide the icon in the status bar: Settings > Apps > Keep Apps 

&#x20;  Awaken > Notifications > disable the "Active Service" channel



Build



Open the folder in Android Studio and use Build > Build APK, or run the following command in the terminal: 

./gradlew assembleRelease.



Credits



Code developed by penkzyyy with the assistance of Claude, by Anthropic.



Roadmap for v2



\- Custom app icon

\- Theme mode: light, dark, or system-default



License



MIT. See the LICENSE file. Feel free to edit, adapt, and redistribute.

