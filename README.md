```markdown
# 🐭 Jogo 2D com LibGDX – Ratventure

Projeto desenvolvido utilizando **Java + LibGDX + Box2D**, estruturado em múltiplas telas e fases, com sistema de física, colisões, HUD, música e progressão entre mapas.

---

## 📌 Descrição

O jogo é um **plataforma 2D** onde o jogador controla um rato que:

- Se movimenta horizontalmente
- Pula quando está no chão
- Coleta queijos para ganhar pontos e vida
- Evita ratoeiras
- Sofre penalidades ao coletar queijo estragado
- Avança por três fases até chegar à tela final

O jogo possui:

- Sistema de física com **Box2D**
- Mapas `.tmx` carregados via **Tiled**
- Sistema de HUD
- Música de fundo
- Tela inicial
- Tela final
- Sistema de Game Over

---

## 🧠 Estrutura do Projeto

### 📂 `screens`

Contém as telas do jogo:

- `StartScreen` → Tela inicial (`telainicial.png`)
- `GameScreen1` → Primeira fase (`esgoto.tmx`)
- `GameScreen2` → Segunda fase (`mapafase2.tmx`)
- `GameScreen3` → Terceira fase (`mapafase3.tmx`)
- `FinalScreen` → Tela final (`telaFinal.jpg`)
- `BaseGameScreen` → Classe abstrata base das fases

A classe `BaseGameScreen` é responsável por:

- Inicializar mundo físico (`World`)
- Configurar câmera e viewport
- Carregar mapas `.tmx`
- Criar corpos físicos do mapa
- Gerenciar HUD
- Gerenciar música (`music.mp3`)
- Detectar colisões
- Gerenciar Game Over
- Controlar transição entre fases

---

### 📂 `entities`

Contém as entidades do jogo:

- `Player`
- `Collectible` (classe abstrata)
- `Cheese`
- `RottenCheese`
- `Trap`

---

## 🎮 Mecânicas do Jogo

### 🐭 Player

- Usa `Box2D`
- Corpo dinâmico com rotação fixa
- Sensor nos pés para detectar chão
- Vida inicial: `100`
- Velocidade máxima controlada
- Pode ser controlado por:
  - Teclado
  - Interface `IPedalController`

#### Controles:

| Tecla | Ação |
|-------|------|
| ← / A | Andar para esquerda |
| → / D | Andar para direita |
| SPACE / ↑ | Pular |
| ENTER | Avançar tela / Reiniciar |

---

### 🧀 Itens Coletáveis

Sistema baseado na classe abstrata `Collectible`.

#### 🟡 Cheese
- +1 ponto
- +10 de vida (até o máximo)

#### 🟤 RottenCheese
- -1 ponto (se houver pontuação)
- Caso não haja pontos, aplica dano

#### ⚠ Trap
- Causa 100 de dano

---

## ❤️ Sistema de Vida

- Vida máxima: `100`
- Dano ratoeira: `100`
- Dano queijo estragado: `10`
- Ganho por queijo: `+10`

Se a vida chegar a 0:

- Perde 5 pontos
- Ativa tela de Game Over
- ENTER reinicia a fase atual

---

## 🗺 Sistema de Mapas

- Mapas carregados com `TmxMapLoader`
- Renderizados com `OrthogonalTiledMapRenderer`
- Colisões criadas automaticamente a partir da camada 2 do mapa
- Gravidade configurada como `-7f`

A câmera segue o jogador e é limitada ao tamanho do mapa.

Ao atingir o final do mapa:
- Avança para próxima fase
- Última fase direciona para `FinalScreen`

---

## 🎵 Áudio

- Música: `music.mp3`
- Loop automático
- Volume configurado em 0.5
- Sistema evita duplicação da música

---

## 🖥 Telas

### ▶ StartScreen
- Exibe `telainicial.png`
- ENTER inicia `GameScreen1`

### 🎮 GameScreens
- 3 fases progressivas
- Cada fase herda `BaseGameScreen`

### 🏁 FinalScreen
- Exibe `telaFinal.jpg`
- ENTER reinicia o jogo
- Pontuação global é resetada

---

## 🔄 Sistema de Colisão

Implementado via `CustomContactListener`.

Detecta:

- Pé do jogador com chão
- Jogador com itens
- Aplica efeitos conforme tipo do item
- Remove corpos físicos corretamente

---

## 📊 HUD

Responsável por:

- Exibir vida atual
- Exibir vida máxima
- Exibir pontuação global

Renderizado com:

- `SpriteBatch`
- `ShapeRenderer`
- Câmera separada da câmera do mundo

---

## 🧩 Tecnologias Utilizadas

- Java
- LibGDX
- Box2D
- Tiled (.tmx)

---

## 📁 Arquivos de Recursos

O projeto utiliza:

- `telainicial.png`
- `telaFinal.jpg`
- `gameover.png`
- `music.mp3`
- `ratoAndar1.png`
- `queijoestragado.png`
- `ratoeira.png`
- `esgoto.tmx`
- `mapafase2.tmx`
- `mapafase3.tmx`

---

## 🚀 Fluxo do Jogo

StartScreen  
⬇  
GameScreen1  
⬇  
GameScreen2  
⬇  
GameScreen3  
⬇  
FinalScreen  

---

## 🧹 Gerenciamento de Recursos

Cada tela implementa `dispose()` corretamente:

- Libera texturas
- Libera música
- Libera mundo físico
- Libera renderizadores
- Evita vazamento de memória

---

## 📌 Observações Técnicas

- PPM (Pixels Per Meter) definido como `90f`
- Mundo físico com `World(new Vector2(0, -7f), true)`
- Player usa interpolação para suavizar velocidade horizontal
- Itens são recriados dinamicamente com `recreateBody()`
- Sistema de respawn aleatório para coletáveis

---

## 📎 Conclusão

O projeto implementa um jogo 2D completo com:

- Estrutura organizada
- Separação clara entre telas
- Sistema de física robusto
- Gerenciamento de colisões
- Progressão entre fases
- HUD funcional
- Sistema de pontuação e vida

Desenvolvido com foco em organização, reutilização de código (via `BaseGameScreen`) e integração entre física e renderização.
```
