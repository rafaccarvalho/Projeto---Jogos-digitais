package br.mackenzie.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;
import java.util.List;

import br.mackenzie.Main;
import br.mackenzie.entities.*;
import br.mackenzie.ui.HUD;
import br.mackenzie.input.IPedalController;

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

    protected IPedalController pedalController;

    // 🎧 Música da fase
    protected Music gameMusic;

    protected int vidaRato;
    protected int VIDA_MAX;
    protected final int VIDA_POR_QUEIJO = 10;
    protected final int DANO_RATOEIRA = 100;
    protected final int DANO_QUEIJO_ESTRAGADO = 10;

    protected boolean gameOver = false;
    protected float gameOverTimer = 0;

    //protected final float MAP_WIDTH_PX = 3900f;
    //protected final float MAP_HEIGHT_PX = 672f;
    protected float mapWidthPx = 0f;
    protected float mapHeightPx = 0f;
    protected final float GRAVITY = -7f;

    protected abstract String getMapPath();
    protected abstract Screen getNextScreen();
    protected abstract BaseGameScreen newInstance();

    // Garante que a música nunca duplica
    private void stopMusicIfPlaying() {
        if (gameMusic != null) {
            try {
                gameMusic.stop();
            } catch (Exception ignored) {}
        }
    }

    public BaseGameScreen(Main game) {
        this.game = game;
    }

    public BaseGameScreen(Main game, IPedalController pedalController) {
        this.game = game;
        this.pedalController = pedalController;
    }

    @Override
    public void show() {

        stopMusicIfPlaying();

        world = new World(new Vector2(0, GRAVITY), true);
        b2dr = new Box2DDebugRenderer();

        camera = new OrthographicCamera();
        viewport = new ExtendViewport(200 / Player.PPM, 200 / Player.PPM, camera);
        viewport.apply();

        // 🎵 Música da fase
        gameMusic = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));
        gameMusic.setLooping(true);
        gameMusic.setVolume(0.5f);
        gameMusic.play();

        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        map = new TmxMapLoader().load(getMapPath());

        try {
            Integer mapTileWidth = (Integer) map.getProperties().get("width");      // número de tiles horizontal
            Integer mapTileHeight = (Integer) map.getProperties().get("height");    // número de tiles vertical
            Integer tilePixelWidth = (Integer) map.getProperties().get("tilewidth");// largura do tile em px
            Integer tilePixelHeight = (Integer) map.getProperties().get("tileheight");// altura do tile em px

            if (mapTileWidth != null && tilePixelWidth != null) {
                mapWidthPx = mapTileWidth * tilePixelWidth;

                mapWidthPx += 3 * tilePixelWidth;
            } else {
                mapWidthPx = 3900f; // fallback
            }

            if (mapTileHeight != null && tilePixelHeight != null) {
                mapHeightPx = mapTileHeight * tilePixelHeight;
            } else {
                mapHeightPx = 672f; // fallback
            }
        } catch (Exception e) {
            Gdx.app.error("BaseGameScreen", "Erro lendo propriedades do mapa, usando valores padrão", e);
            mapWidthPx = 3900f;
            mapHeightPx = 672f;
        }

        mapRenderer = new OrthogonalTiledMapRenderer(map, 1f / Player.PPM);

        createMapBodiesFromLayer2();

        float playerStartX = 200f, playerStartY = 120f;
        player = new Player(world, playerStartX, playerStartY);
        vidaRato = player.getVidaAtual();
        VIDA_MAX = player.getVidaMax();

        collectibles = new ArrayList<>();

        int numInitialCollectibles = 15;
        for (int i = 0; i < numInitialCollectibles; i++) {
            spawnRandomCollectible();
        }

        hud = new HUD();
        gameOverTexture = new Texture("gameover.png");

        gameContactListener = new CustomContactListener(this);
        world.setContactListener(gameContactListener);

        camera.position.set(playerStartX / Player.PPM, playerStartY / Player.PPM, 0);
        camera.update();
    }

    private void spawnRandomCollectible() {
        int randomValue = MathUtils.random(99);

        Collectible newCollectible;
        float tempX = 100f, tempY = 150f;

        if (randomValue < 40) newCollectible = new Cheese(world, tempX, tempY);
        else if (randomValue < 70) newCollectible = new RottenCheese(world, tempX, tempY);
        else newCollectible = new Trap(world, tempX, tempY);

        newCollectible.respawnRandom();
        collectibles.add(newCollectible);
    }

    public void applyDamage(int damage) {
        if (damage == DANO_QUEIJO_ESTRAGADO && game.pontuacaoGlobal > 0) {
            game.pontuacaoGlobal--;
            return;
        }

        vidaRato -= damage;

        if (vidaRato <= 0) {
            game.pontuacaoGlobal = Math.max(0, game.pontuacaoGlobal - 5);

            gameOver = true;
            gameOverTimer = 1.5f;
        }
    }

    private void createMapBodiesFromLayer2() {
        BodyDef bdef = new BodyDef();
        PolygonShape shape = new PolygonShape();
        FixtureDef fdef = new FixtureDef();

        if (map.getLayers().getCount() <= 2) return;

        for (RectangleMapObject object : map.getLayers().get(2).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = object.getRectangle();
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

            if (pedalController != null) {
                pedalController.update(delta);
                player.setPedalVelocity(pedalController.getPedalValue());
            }

            for (Collectible item : collectibles)
                item.update();

            world.step(1 / 60f, 6, 2);

            gameContactListener.processDestructions();

            player.update(Gdx.graphics.getDeltaTime());

            if (vidaRato <= 0) {
                gameOver = true;
                gameOverTimer = 1.5f;
            }

        } else {
            gameOverTimer -= delta;
            if (gameOverTimer <= 0 && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {

                stopMusicIfPlaying();

                game.setScreen(newInstance());
                return;
            }
        }

        updateCamera();

        float playerX = player.getBody().getPosition().x * Player.PPM;
        float exitThreshold = Math.max(100f, mapWidthPx - 100f); // evita threshold incorreto se mapa muito pequeno
        if (playerX >= exitThreshold) {
            stopMusicIfPlaying();
            Screen next = getNextScreen();
            if (next != null) {
                game.setScreen(next);
            } else {
                game.setScreen(new FinalScreen(game));
            }
            dispose();
            return;
        }

        if (player.getBody().getPosition().y < -2f) {
            game.pontuacaoGlobal = Math.max(0, game.pontuacaoGlobal - 5);
            vidaRato = 0;
        }

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
            hud.drawHUD(batch, shapeRenderer, viewport, hudCamera, vidaRato, VIDA_MAX, game.pontuacaoGlobal);
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }
    }

    private void updateCamera() {
        viewport.apply();
        float mapWidth = mapWidthPx / Player.PPM;
        float mapHeight = mapHeightPx / Player.PPM;
        float halfWidth = viewport.getWorldWidth() / 2f;
        float halfHeight = viewport.getWorldHeight() / 2f;

        float targetX = Math.max(halfWidth, Math.min(player.getBody().getPosition().x, mapWidth - halfWidth));
        float targetY = Math.max(halfHeight, Math.min(player.getBody().getPosition().y, mapHeight - halfHeight));

        camera.position.set(targetX, targetY, 0);
        camera.update();
        mapRenderer.setView(camera);
    }

    @Override
    public void resize(int w, int h) { viewport.update(w, h, true); }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {

        stopMusicIfPlaying();

        if (gameMusic != null) {
            gameMusic.dispose();
            gameMusic = null;
        }

        batch.dispose();
        if (gameOverTexture != null) gameOverTexture.dispose();
        if (hud != null) hud.dispose();
        if (world != null) world.dispose();
        if (map != null) map.dispose();
        if (b2dr != null) b2dr.dispose();
        if (mapRenderer != null) mapRenderer.dispose();
        if (pedalController != null) pedalController.dispose();
    }


    // -------------------- CONTACT LISTENER --------------------

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
                    screen.game.pontuacaoGlobal++;
                    screen.vidaRato = Math.min(screen.VIDA_MAX, screen.vidaRato + screen.VIDA_POR_QUEIJO);
                    c.ativo = false;
                    itemsToRemove.add(c);
                }
            }

            else if (item instanceof RottenCheese) {
                RottenCheese rc = (RottenCheese) item;
                if (rc.ativo) {
                    screen.applyDamage(screen.DANO_QUEIJO_ESTRAGADO);
                    rc.ativo = false;
                    itemsToRemove.add(rc);
                }
            }

            else if (item instanceof Trap) {
                Trap t = (Trap) item;
                if (t.ativo) {
                    screen.applyDamage(screen.DANO_RATOEIRA);
                    t.ativo = false;
                }
            }
        }

        @Override
        public void endContact(Contact contact) {
            Fixture a = contact.getFixtureA();
            Fixture b = contact.getFixtureB();
            Object da = a.getUserData(), db = b.getUserData();

            if ((da == null || db == null)) return;

            if ((da.equals("foot") || db.equals("foot")) &&
                (da.equals("ground") || db.equals("ground"))) {
                screen.player.setGrounded(false);
            }
        }

        @Override public void preSolve(Contact contact, Manifold oldManifold) {}
        @Override public void postSolve(Contact contact, ContactImpulse impulse) {}
    }
}
