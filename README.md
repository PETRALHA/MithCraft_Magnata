# MithCraft_Magnata
## 📜 Licença

Este projeto está licenciado sob a **MIT License** - veja o arquivo [https://github.com/PETRALHA/MithCraft_Magnata/blob/main/LICENSE](LICENSE) para detalhes.

MithCraft Magnata - Plugin para Minecraft
📝 Descrição
O MithCraft Magnata é um plugin para servidores Minecraft (Spigot/Paper/Purpur) que adiciona um sistema automático de Magnata, identificando e premiando o jogador mais rico do servidor.

Ele se integra com EssentialsX (para economia) e LuckPerms (para grupos e permissões), permitindo personalizar recompensas, mensagens e comandos conforme a necessidade do servidor.

🔧 Compatibilidade
Versão do Minecraft	Suporte
1.21 (Atual)	✅ Total
1.20.x	✅ Testado
1.19.x	⚠️ Possível
1.18.x e abaixo	❌ Não testado
Nota:

Funciona em Spigot, Paper e Purpur (Bukkit API).

Depende do EssentialsX (para economia) e LuckPerms (para grupos).

Se o seu servidor não usa EssentialsX, o plugin não funcionará (a menos que use outro sistema de economia compatível com a Vault API).

🚀 Features
✅ Sistema Automático de Magnata

Verifica periodicamente o jogador mais rico e atualiza o grupo no LuckPerms.

Permite configurar prefixos, permissões e recompensas para o Magnata.

✅ Comandos Personalizáveis

/magnata → Mostra o jogador mais rico do servidor e sua fortuna.

/magnata hist → Exibe o histórico de Magnatas anteriores.

Você pode alterar os comandos no config.yml!

✅ Mensagens Customizáveis

Broadcast quando um novo Magnata é definido.

Mensagens multi-linha no chat.

Tudo configurável no arquivo config.yml.

✅ Recompensas Automáticas

Recompensas ao se tornar Magnata (itens, money, efeitos).

Recompensas periódicas (enquanto permanece Magnata).

✅ Histórico de Magnatas

Armazena os últimos Magnatas do servidor.

Mostra nome, data e fortuna de cada um.

⚙️ Dependências Obrigatórias
Plugin	Versão Testada	Descrição
EssentialsX	2.20+	Fornece o sistema de economia.
LuckPerms	5.4+	Gerencia grupos e permissões.
Se não tiver esses plugins, o MithCraft Magnata não funcionará!

📥 Download & Instalação
Baixe a versão mais recente na página de Releases.

Coloque o .jar na pasta plugins/ do seu servidor.

Reinicie o servidor para gerar o config.yml.

Edite o config.yml para personalizar mensagens, comandos e recompensas.