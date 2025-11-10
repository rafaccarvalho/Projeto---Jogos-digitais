package br.mackenzie.screens;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import java.util.ArrayList;
import java.util.List;

import br.mackenzie.Main;
import br.mackenzie.entities.*;
import br.mackenzie.ui.HUD;

public class GameScreen implements Screen {

    private final Main game;
    private OrthographicCamera hudCamera;
    private OrthographicCamera camera;
    private ExtendViewport viewport;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;

    // Map
    private TmxMapLoader mapLoader;
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;

    // Box2D
    private World world;
    private Box2DDebugRenderer b2dr;

    private CustomContactListener gameContactListener;

    // Entities
    private Player player;
    private Cheese cheese;
    private RottenCheese rotten;
    private Trap trap;
    // Lista para gerenciar todos os objetos que podem ser coletados/destruídos
    private List<Collectible> collectibles;

    // HUD / status
    private HUD hud;
    private Texture gameOverTexture;
    private int pontuacao = 0;
    private int vidaRato = 100;
    private final int VIDA_MAX = 100;
    private final int VIDA_POR_QUEIJO = 20;
    private final int DANO_RATOEIRA = 100;
    private final int DANO_QUEIJO_ESTRAGADO = 10;
    private boolean gameOver = false;
    private float gameOverTimer = 0;

    // Mapa (em Pixels)
    private final float MAP_WIDTH_PX = 3900f;
    private final float MAP_HEIGHT_PX = 672f;
    private final float GRAVITY = -10f;
    // Tamanho da tela (mais fácil de editar)
    private static final float WORLD_WIDTH = 1280;
    private static final float WORLD_HEIGHT = 720;


    public GameScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        world = new World(new Vector2(0, GRAVITY), true);
        b2dr = new Box2DDebugRenderer();

        camera = new OrthographicCamera();
        // viewport = new StretchViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport = new ExtendViewport(200 / Player.PPM, 200 / Player.PPM, camera);
        viewport.apply();

        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        mapLoader = new TmxMapLoader();
        map = mapLoader.load("esgoto.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1f / Player.PPM);

        createMapBodiesFromLayer2();

        float playerStartX = 200f, playerStartY = 120f;
        player = new Player(world, playerStartX, playerStartY);

        cheese = new Cheese(world, playerStartX + 600f, playerStartY + 20f);
        rotten = new RottenCheese(world, playerStartX + 900f, playerStartY + 20f);
        trap = new Trap(world, playerStartX + 1200f, playerStartY);

        // Inicializa a lista de colecionáveis
        collectibles = new ArrayList<>();
        collectibles.add(cheese);
        collectibles.add(rotten);
        collectibles.add(trap);

        hud = new HUD();
        gameOverTexture = new Texture("gameover.png");

        gameContactListener = new CustomContactListener(this);
        world.setContactListener(gameContactListener);

        camera.position.set(playerStartX / Player.PPM, playerStartY / Player.PPM, 0);
        camera.update();
    }

    private void createMapBodiesFromLayer2() {
        BodyDef bdef = new BodyDef();
        PolygonShape shape = new PolygonShape();
        FixtureDef fdef = new FixtureDef();

        if (map.getLayers().getCount() <= 2) {
            Gdx.app.error("GameScreen", "Layer 2 não existe no mapa. Colisão do chão pode falhar.");
            return;
        }

        for (MapObject object : map.getLayers().get(2).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) object).getRectangle();

            bdef.type = BodyDef.BodyType.StaticBody;
            bdef.position.set((rect.getX() + rect.getWidth() / 2f) / Player.PPM,
                (rect.getY() + rect.getHeight() / 2f) / Player.PPM);

            Body body = world.createBody(bdef);
            shape.setAsBox((rect.getWidth() / 2f) / Player.PPM, (rect.getHeight() / 2f) / Player.PPM);

            fdef.shape = shape;
            fdef.friction = 0.8f;
            body.createFixture(fdef).setUserData("ground");
        }

        shape.dispose();
    }


    @Override
    public void render(float delta) {
        // --- Atualização lógica ---
        if (!gameOver) {
            player.handleInput();
            player.update(Gdx.graphics.getDeltaTime());

            for (Collectible item : collectibles) {
                item.update();
            }
            //player.update();

            world.step(1 / 60f, 6, 2);
            gameContactListener.processDestructions();

            if (vidaRato <= 0) {
                gameOver = true;
                gameOverTimer = 1.5f;
            }
        } else {
            gameOverTimer -= delta;
            if (gameOverTimer <= 0 && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                game.setScreen(new GameScreen(game));
            }
        }
        updateCamera();

        // --- Checa se o jogador chegou ao fim do mapa ---
        float playerX = player.getBody().getPosition().x * Player.PPM; // posição em pixels
        if (playerX >= MAP_WIDTH_PX - 100) { // chegou perto do fim
            game.setScreen(new GameScreen2(game)); // vai pra próxima fase
            dispose();
            return;
        }

        // --- Renderiza o mapa e entidades ---
        ScreenUtils.clear(Color.BLACK);
        // mapRenderer.setView(camera);
        mapRenderer.render();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        for (Collectible item : collectibles) {
            item.draw(batch);
        }
        player.draw(batch);

        if (gameOver) {
            float vx = camera.position.x - viewport.getWorldWidth() / 2f;
            float vy = camera.position.y - viewport.getWorldHeight() / 2f;
            batch.draw(gameOverTexture, vx, vy, viewport.getWorldWidth(), viewport.getWorldHeight());
        }

        batch.end();

        // --- Renderização do HUD ---
        if(!gameOver) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            hudCamera.update();


            // Define projeção fixa (tela)
            batch.setProjectionMatrix(hudCamera.combined);
            shapeRenderer.setProjectionMatrix(hudCamera.combined);

            // Desenha o HUD (barra e textos)
            hud.drawHUD(batch, shapeRenderer, viewport, hudCamera, vidaRato, VIDA_MAX, pontuacao);

            Gdx.gl.glDisable(GL20.GL_BLEND);
        }

        // --- Debug Box2D ---
        b2dr.render(world, camera.combined);
    }




    private void resetGame() {
        // Para uma reinicialização LIMPA, o ideal é recriar o mundo e as entidades
        this.dispose();
        this.show();
    }

    private void updateCamera() {
        viewport.apply();
        float mapWidth = MAP_WIDTH_PX / Player.PPM;
        float mapHeight = MAP_HEIGHT_PX / Player.PPM;

        float halfWidth = viewport.getWorldWidth() / 2f;
        float halfHeight = viewport.getWorldHeight() / 2f;

        float targetX = player.getBody().getPosition().x;
        float targetY = player.getBody().getPosition().y;

        targetX = Math.max(halfWidth, Math.min(targetX, mapWidth - halfWidth));
        targetY = Math.max(halfHeight, Math.min(targetY, mapHeight - halfHeight));

        camera.position.set(targetX, targetY, 0);
        camera.update();
        mapRenderer.setView(camera);
    }

    @Override
    public void resize(int w, int h) {
        viewport.update(w, h, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        if (gameOverTexture != null) gameOverTexture.dispose();
        player.dispose();
        cheese.dispose();
        rotten.dispose();
        trap.dispose();
        hud.dispose();
        if (world != null) world.dispose();
        if (map != null) map.dispose();
        if (b2dr != null) b2dr.dispose();
        if (mapRenderer != null) mapRenderer.dispose();
        // Não precisamos destruir explicitamente os bodies se chamarmos world.dispose()
    }
    private static class CustomContactListener implements ContactListener {
        private final GameScreen screen;
        // Lista de Collectible que devem ser destruídos no próximo passo
        private final List<Collectible> itemsToRemove = new ArrayList<>();

        public CustomContactListener(GameScreen screen) {
            this.screen = screen;
        }

        // Chamado APÓS world.step()
        public void processDestructions() {
            for (Collectible item : itemsToRemove) {
                if (item.getBody() != null) {
                    // 🔒 desativa antes de destruir
                    item.getBody().setActive(false);
                    screen.world.destroyBody(item.getBody());
                    item.body = null;
                }
            }
            itemsToRemove.clear();
        }

        @Override
        public void beginContact(Contact contact) {
            Fixture fixA = contact.getFixtureA();
            Fixture fixB = contact.getFixtureB();
            Object dataA = fixA.getUserData();
            Object dataB = fixB.getUserData();

            if (dataA == null || dataB == null) return;

            // --- Player/Foot vs Ground ---
            boolean isFoot = dataA.equals("foot") || dataB.equals("foot");
            boolean isGround = dataA.equals("ground") || dataB.equals("ground");

            if (isFoot && isGround) {
                screen.player.setGrounded(true);
            }

            // --- Player vs Collectible ---
            boolean isPlayerBody = dataA.equals("player") || dataB.equals("player");
            if (!isPlayerBody) return;

            Collectible item = null;
            if (dataA instanceof Collectible) item = (Collectible) dataA;
            if (dataB instanceof Collectible) item = (Collectible) dataB;

            if (item == null) return;

            if (item instanceof Cheese) {
                Cheese c = (Cheese) item;
                if (c.ativo) {
                    screen.pontuacao++;
                    screen.vidaRato = Math.min(screen.VIDA_MAX, screen.vidaRato + screen.VIDA_POR_QUEIJO);

                    // 🔒 não destruir agora — apenas marcar e adicionar à fila
                    c.ativo = false;
                    itemsToRemove.add(c);
                }
            } else if (item instanceof RottenCheese) {
                RottenCheese rc = (RottenCheese) item;
                if (rc.ativo) {
                    screen.vidaRato -= screen.DANO_QUEIJO_ESTRAGADO;
                    rc.ativo = false;
                    itemsToRemove.add(rc);
                }
            } else if (item instanceof Trap) {
                Trap t = (Trap) item;
                if (t.ativo) {
                    screen.vidaRato = 0;
                    t.ativo = false;
                    // traps não são removidas
                }
            }

            if (screen.vidaRato <= 0) {
                screen.gameOver = true;
                screen.gameOverTimer = 1.5f;

            }
        }

        @Override
        public void endContact(Contact contact) {
            Fixture fixA = contact.getFixtureA();
            Fixture fixB = contact.getFixtureB();
            Object dataA = fixA.getUserData();
            Object dataB = fixB.getUserData();
            if (dataA == null || dataB == null) return;

            boolean isFoot = dataA.equals("foot") || dataB.equals("foot");
            boolean isGround = dataA.equals("ground") || dataB.equals("ground");

            if (isFoot && isGround) {
                screen.player.setGrounded(false);
            }
        }

        @Override public void preSolve(Contact contact, Manifold oldManifold) {}
        @Override public void postSolve(Contact contact, ContactImpulse impulse) {}
    }
}
