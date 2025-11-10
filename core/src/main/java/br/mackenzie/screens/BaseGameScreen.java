package br.mackenzie.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
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

public abstract class BaseGameScreen implements Screen {

    protected final Main game;
    protected OrthographicCamera hudCamera;
    protected OrthographicCamera camera;
    protected ExtendViewport viewport;
    protected SpriteBatch batch;
    protected ShapeRenderer shapeRenderer;

    protected TiledMap map;
    protected OrthogonalTiledMapRenderer mapRenderer;
    protected World world;
    protected Box2DDebugRenderer b2dr;

    protected CustomContactListener gameContactListener;
    protected Player player;
    protected List<Collectible> collectibles;

    protected HUD hud;
    protected Texture gameOverTexture;

    protected int pontuacao = 0;
    protected int vidaRato;
    protected int VIDA_MAX;
    protected final int VIDA_POR_QUEIJO = 20;
    protected final int DANO_RATOEIRA = 100;
    protected final int DANO_QUEIJO_ESTRAGADO = 10;

    protected boolean gameOver = false;
    protected float gameOverTimer = 0;

    protected final float MAP_WIDTH_PX = 3900f;
    protected final float MAP_HEIGHT_PX = 672f;
    protected final float GRAVITY = -10f;
    protected static final float WORLD_WIDTH = 1280;
    protected static final float WORLD_HEIGHT = 720;

    // 🔹 Cada fase define o nome do mapa aqui
    protected abstract String getMapPath();

    // 🔹 Cada fase pode dizer qual a próxima fase (ou null se for a última)
    protected abstract Screen getNextScreen();

    public BaseGameScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        world = new World(new Vector2(0, GRAVITY), true);
        b2dr = new Box2DDebugRenderer();

        camera = new OrthographicCamera();
        viewport = new ExtendViewport(200 / Player.PPM, 200 / Player.PPM, camera);
        viewport.apply();

        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        // --- Carrega o mapa da fase ---
        TmxMapLoader mapLoader = new TmxMapLoader();
        map = mapLoader.load(getMapPath());
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1f / Player.PPM);

        createMapBodiesFromLayer2();

        float playerStartX = 200f, playerStartY = 120f;
        player = new Player(world, playerStartX, playerStartY);
        vidaRato = player.getVida();
        VIDA_MAX = player.getVidaMax();

        collectibles = new ArrayList<>();
        collectibles.add(new Cheese(world, playerStartX + 600f, playerStartY + 20f));
        collectibles.add(new RottenCheese(world, playerStartX + 900f, playerStartY + 20f));
        collectibles.add(new Trap(world, playerStartX + 1200f, playerStartY));

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
        if (!gameOver) {
            player.handleInput();
            player.update(Gdx.graphics.getDeltaTime());
            for (Collectible item : collectibles) item.update();
            world.step(1 / 60f, 6, 2);
            gameContactListener.processDestructions();

            if (vidaRato <= 0) {
                gameOver = true;
                gameOverTimer = 1.5f;
            }
        } else {
            gameOverTimer -= delta;
            if (gameOverTimer <= 0 && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                game.setScreen(newInstance());
            }
        }

        updateCamera();

        // --- Checa se o jogador chegou ao fim do mapa ---
        float playerX = player.getBody().getPosition().x * Player.PPM;
        if (playerX >= MAP_WIDTH_PX - 100) {
            Screen next = getNextScreen();
            if (next != null) {
                game.setScreen(next);
                dispose();
                return;
            }
        }

        // --- Checa se o jogador caiu ---
        if (player.getBody().getPosition().y < -2f) {
            vidaRato = 0;
        }

        // --- Renderização ---
        ScreenUtils.clear(Color.BLACK);
        mapRenderer.render();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (Collectible item : collectibles) item.draw(batch);
        player.draw(batch);

        if (gameOver) {
            float vx = camera.position.x - viewport.getWorldWidth() / 2f;
            float vy = camera.position.y - viewport.getWorldHeight() / 2f;
            batch.draw(gameOverTexture, vx, vy, viewport.getWorldWidth(), viewport.getWorldHeight());
        }
        batch.end();

        if (!gameOver) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            hudCamera.update();
            batch.setProjectionMatrix(hudCamera.combined);
            shapeRenderer.setProjectionMatrix(hudCamera.combined);
            hud.drawHUD(batch, shapeRenderer, viewport, hudCamera, vidaRato, VIDA_MAX, pontuacao);
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }

        // b2dr.render(world, camera.combined);
    }

    private void updateCamera() {
        viewport.apply();
        float mapWidth = MAP_WIDTH_PX / Player.PPM;
        float mapHeight = MAP_HEIGHT_PX / Player.PPM;
        float halfWidth = viewport.getWorldWidth() / 2f;
        float halfHeight = viewport.getWorldHeight() / 2f;

        float targetX = Math.max(halfWidth, Math.min(player.getBody().getPosition().x, mapWidth - halfWidth));
        float targetY = Math.max(halfHeight, Math.min(player.getBody().getPosition().y, mapHeight - halfHeight));

        camera.position.set(targetX, targetY, 0);
        camera.update();
        mapRenderer.setView(camera);
    }

    protected abstract BaseGameScreen newInstance();

    @Override public void resize(int w, int h) { viewport.update(w, h, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        if (gameOverTexture != null) gameOverTexture.dispose();
        player.dispose();
        if (hud != null) hud.dispose();
        if (world != null) world.dispose();
        if (map != null) map.dispose();
        if (b2dr != null) b2dr.dispose();
        if (mapRenderer != null) mapRenderer.dispose();
    }

    // 🔹 Listener interno mantido igual
    protected static class CustomContactListener implements ContactListener {
        private final BaseGameScreen screen;
        private final List<Collectible> itemsToRemove = new ArrayList<>();

        public CustomContactListener(BaseGameScreen screen) {
            this.screen = screen;
        }

        public void processDestructions() {
            for (Collectible item : itemsToRemove) {
                if (item.getBody() != null) {
                    item.getBody().setActive(false);
                    screen.world.destroyBody(item.getBody());
                    item.body = null;
                }
            }
            itemsToRemove.clear();
        }

        @Override
        public void beginContact(Contact contact) {
            Fixture a = contact.getFixtureA();
            Fixture b = contact.getFixtureB();
            Object da = a.getUserData(), db = b.getUserData();
            if (da == null || db == null) return;

            boolean isFoot = da.equals("foot") || db.equals("foot");
            boolean isGround = da.equals("ground") || db.equals("ground");
            if (isFoot && isGround) screen.player.setGrounded(true);

            boolean isPlayer = da.equals("player") || db.equals("player");
            if (!isPlayer) return;

            Collectible item = null;
            if (da instanceof Collectible) item = (Collectible) da;
            if (db instanceof Collectible) item = (Collectible) db;
            if (item == null) return;

            if (item instanceof Cheese) {
                Cheese c = (Cheese) item;
                if (c.ativo) {
                    screen.pontuacao++;
                    screen.vidaRato = Math.min(screen.VIDA_MAX, screen.vidaRato + screen.VIDA_POR_QUEIJO);

                    // não destruir agora — apenas marcar e adicionar à fila
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

        @Override public void endContact(Contact contact) {
            Fixture a = contact.getFixtureA(), b = contact.getFixtureB();
            Object da = a.getUserData(), db = b.getUserData();
            if (da == null || db == null) return;
            if ((da.equals("foot") || db.equals("foot")) && (da.equals("ground") || db.equals("ground"))) {
                screen.player.setGrounded(false);
            }
        }

        @Override public void preSolve(Contact contact, Manifold oldManifold) {}
        @Override public void postSolve(Contact contact, ContactImpulse impulse) {}
    }
}
