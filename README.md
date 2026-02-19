# 🐭 Ratventure

Jogo 2D desenvolvido em **Java** utilizando **LibGDX** e **Box2D**, com múltiplas fases, sistema de pontuação, vida, colisões físicas e tela final.

O jogador controla um rato que deve atravessar o mapa, coletar queijos, evitar armadilhas e chegar ao final da fase.

---

# 🎮 Funcionalidades

- Sistema de múltiplas fases
- Física com Box2D
- Sistema de vida
- Sistema de pontuação global
- Itens coletáveis aleatórios
- Dano por armadilhas e queijo estragado
- HUD com vida e pontuação
- Tela inicial
- Tela de Game Over
- Tela Final
- Música de fundo em loop
- Controle por teclado
- Suporte a controlador externo via `IPedalController`

---

# 🕹️ Controles

## Teclado

- `←` ou `A` → mover para esquerda  
- `→` ou `D` → mover para direita  
- `SPACE` ou `↑` → pular  

## ENTER

- Iniciar jogo (na tela inicial)  
- Reiniciar fase (Game Over)  
- Reiniciar jogo (tela final)  

---

# 🧠 Mecânicas do Jogo

## 🧀 Coletáveis

O jogo possui três tipos de itens gerados aleatoriamente:

### Cheese
- +1 ponto
- +10 de vida

### RottenCheese
- -1 ponto (se pontuação > 0)

### Trap
- -100 de vida

São gerados **15 itens iniciais por fase**, com respawn em posições aleatórias.

---

# ❤️ Sistema de Vida

- Vida inicial: 100
- Vida máxima: 100
- Vida ao coletar queijo: +10
- Dano ratoeira: 100
- Dano queijo estragado: 10

Se a vida chegar a 0:
- O jogador perde 5 pontos (mínimo 0)
- Tela de Game Over é exibida
- Pode reiniciar pressionando ENTER

---

# 🏁 Progressão de Fases

O jogo possui 3 fases:

1. GameScreen1 → `esgoto.tmx`
2. GameScreen2 → `mapafase2.tmx`
3. GameScreen3 → `mapafase3.tmx`

Ao alcançar o final do mapa:
- Avança para a próxima fase
- Na última fase, vai para a FinalScreen

---

# 🗺️ Estrutura de Telas

- StartScreen
- GameScreen1
- GameScreen2
- GameScreen3
- FinalScreen

Todas as fases herdam de:

BaseGameScreen

Responsável por:
- Mundo Box2D
- Renderização do mapa TMX
- Câmera
- HUD
- Música
- Sistema de colisão
- Spawn de coletáveis
- Lógica de Game Over

---

# ⚙️ Arquitetura

## Pacotes

```
br.mackenzie
 ├── screens
 ├── entities
 ├── ui
 └── input
```

---

# 👤 Player

- Corpo dinâmico no Box2D
- Sensor nos pés para detectar chão
- Pulo com impulso vertical
- Movimento horizontal limitado
- Suporte a controle externo via IPedalController
- Sprite sincronizado com corpo físico

---

# 🧱 Física

- Gravidade: -7
- Conversão Pixel → Metro: PPM = 90
- Colisões tratadas com CustomContactListener
- Objetos do mapa criados a partir da camada 2 do `.tmx`

---

# 🎵 Áudio

- Música: music.mp3
- Loop ativado
- Volume: 0.5
- Garantia de não duplicação entre telas

---

# 🖼️ Assets Utilizados

- telainicial.png
- telaFinal.jpg
- gameover.png
- ratoAndar1.png
- queijoestragado.png
- ratoeira.png
- esgoto.tmx
- mapafase2.tmx
- mapafase3.tmx
- music.mp3

---

# 🚀 Como Executar

1. Importar o projeto em uma IDE compatível com LibGDX
2. Garantir que os assets estejam na pasta correta
3. Executar a classe principal do projeto (`Main`)

---

# 📌 Observações Técnicas

- Viewport principal: ExtendViewport
- Tela inicial usa StretchViewport
- Sistema de câmera acompanha o jogador com limites do mapa
- Caso o jogador caia do mapa:
  - Perde 5 pontos
  - Vida vai para 0
- O jogo utiliza OrthogonalTiledMapRenderer para renderização do mapa

---

# 🏆 Fluxo do Jogo

1. Tela inicial (ENTER)
2. Fase 1
3. Fase 2
4. Fase 3
5. Tela Final (ENTER reinicia)

---

# 📄 Licença

Projeto acadêmico desenvolvido para fins educacionais.
