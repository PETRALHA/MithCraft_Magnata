<h1 align="center">
  <br>
  <a href="http://www.MithCraft.com.br"><img src="https://github.com/user-attachments/assets/056da34d-f79e-4101-a20e-febd537da3ac"
 alt="Magnata" width="200"></a>
</h1>

# 🏦 MithCraft Magnata - Sistema completo de Magnatas para Minecraft

![GitHub release](https://img.shields.io/github/v/release/PETRALHA/MithCraft_Magnata?style=for-the-badge)
![GitHub license](https://img.shields.io/github/license/PETRALHA/MithCraft_Magnata?style=for-the-badge)

O plugin definitivo para economia em servidores Minecraft, identificando automaticamente o jogador mais rico e oferecendo um sistema completo de recompensas e reconhecimento.

## ✨ Recursos Principais

- 🤑 **Identificação automática** do jogador mais rico (magnata)
- 🎁 **Sistema de recompensas** totalmente configurável
- 📜 **Histórico completo** com paginação
- 💬 **Mensagens 100% personalizáveis** via messages.yml
- 🔗 **Integrações nativas** com Vault, LuckPerms e PlaceholderAPI
- 📊 **15+ placeholders** para uso em outros plugins
- ⚡ **Otimizado** para servidores Purpur 1.21+

## 📥 Instalação

1. Baixe a versão mais recente na aba [Releases](https://github.com/PETRALHA/MithCraft_Magnata/releases)
2. Coloque o arquivo `.jar` na pasta `plugins/`
3. Reinicie o servidor
4. Configure conforme necessário no arquivo `plugins/MithCraftMagnata/config.yml`

## ⚙️ Configuração Básica

```yaml
# plugins/MithCraftMagnata/config.yml
settings:
  check_interval: 300 # Verificação a cada 5 minutos

rewards:
  on_become:
    - "eco give %player% 100"  # Concede $100
    - "lp user %player% parent add magnata"  # Adiciona grupo magnata
    - "lp user %magnata_previous_player% parent remove magnata"  # Remove do magnata anterior
  
  periodic:
    interval: 60 # Recompensas a cada 1 hora
    commands:
      - "eco give %player% 500"
```

## 📌 Placeholders (PlaceholderAPI)

### Básicos
| Placeholder          | Descrição                     | Exemplo         |
|----------------------|-------------------------------|-----------------|
| `%magnata_name%`     | Nome do magnata atual         | `Steve`         |
| `%magnata_balance%`  | Fortuna formatada             | `$1,000.00`     |

### Ranking
| Placeholder            | Descrição                     |
|------------------------|-------------------------------|
| `%magnata_position_1%` | Jogador na 1ª posição         |
| `%magnata_top_3_line%` | Linha formatada do top 3      |

[Ver lista completa de placeholders](#placeholders-completos)

## 🎮 Comandos

| Comando               | Descrição                     | Permissão          |
|-----------------------|-------------------------------|--------------------|
| `/magnata`            | Mostra o magnata atual        | `magnata.command`  |
| `/magnata history [página]` | Ver histórico           | `magnata.history`  |
| `/magnata reload`     | Recarregar configurações      | `magnata.reload`   |
| `/magnata help`     | Mostra menu de Ajuda      | `magnata.help`   |

**Aliases:** `/magnata ajuda`, `/magnata ?`, `/magnata hist`, `/magnata list`

## 📚 Sistema de Permissões

- `magnata.command` - Acesso ao comando base
- `magnata.history` - Ver histórico
- `magnata.reload` - Recarregar plugin
- `magnata.notify` - Receber notificações
- `magnata.help` - Ver ajuda
- `magnata.admin` - Acesso completo

## 🌟 Destaques

✅ **Totalmente configurável** - Adapte cada mensagem e recompensa  
✅ **Baixo impacto no servidor** - Verificações otimizadas  
✅ **Suporte multi-economia** - Funciona com qualquer sistema via Vault  
✅ **Documentação completa** - Fácil configuração  

## 🖼️ Screenshots
![2025-07-01](https://github.com/user-attachments/assets/985d49a2-038f-4830-a253-38fa465fa0ed)
![2025-07-01_20 43 59](https://github.com/user-attachments/assets/29ee9b5e-91d1-478c-b759-14a18ba859e0)
![2025-07-01_20 43 33](https://github.com/user-attachments/assets/75409490-efba-48d9-bec5-76e91156daaf)


## 🔗 Links Úteis

- [Reportar Bug](https://github.com/PETRALHA/MithCraft_Magnata/issues)
- [Solicitar Feature](https://github.com/PETRALHA/MithCraft_Magnata/discussions)
- [Discord](https://discord.gg/xa5bux8rvJ)
- [Spigot](https://www.spigotmc.org/resources/magnata-mithcraft-plugins.126580/)

---

<details>
<summary><h2 id="placeholders-completos">📋 Placeholders Completos</h2></summary>

### 🔹 Básicos
| Placeholder              | Descrição                                  |
|--------------------------|-------------------------------------------|
| `%magnata_name%`         | Nome do magnata atual                     |
| `%magnata_uuid%`      | UUID do magnata atual              |
| `%magnata_balance%`      | Fortuna formatada do magnata              |
| `%magnata_date%`      | Data que se tornou magnata              |
| `%magnata_count%`      | Total de jogadores no histórico              |

### 🔹 Histórico
| Placeholder                  | Descrição                          |
|------------------------------|-----------------------------------|
| `%magnata_previous_name%`    | Nome do magnata anterior          |
| `%magnata_previous_uuid%`    | UUID do magnata anterior          |
| `%magnata_previous_balance%`    | Fortuna do magnata anterior          |
| `%magnata_previous_date%`    | Data que foi magnata          |

### 🔹 Ranking
| Placeholder            | Descrição                     |
|------------------------|-------------------------------|
| `%magnata_position_X%` | Jogador na posição X          |
| `%magnata_balance_X%` | Fortuna do jogador na posição X          |
| `%magnata_date_X%` | Data que assumiu a posição X          |
| `%magnata_top_X_line%` | Linha formatada do ranking    |

</details>
```