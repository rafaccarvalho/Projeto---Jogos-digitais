package br.mackenzie.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Player {
    private final Texture texture;
    private final Sprite sprite;
    private boolean isJumping = false;
    private final float jumpSpeed = 6;
    private final float gravity = -0.10f;
    private float velocityY = 0;
    private final float groundY = 100;
    private final float worldWidth = 3000;

    public Player() {
        texture = new Texture("ratoAndar1.png");
        sprite = new Sprite(texture);
        sprite.setSize(100, 55);
        sprite.setPosition(200, groundY);
    }

    public void handleInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            sprite.translateX(2);
            if (sprite.isFlipX()) sprite.flip(true, false);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            sprite.translateX(-2);
            if (!sprite.isFlipX()) sprite.flip(true, false);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && !isJumping) {
            isJumping = true;
            velocityY = jumpSpeed;
        }
    }

    public void updateJump() {
        if (isJumping) {
            sprite.translateY(velocityY);
            velocityY += gravity;
            if (sprite.getY() <= groundY) {
                sprite.setY(groundY);
                isJumping = false;
                velocityY = 0;
            }
        }

        if (sprite.getX() < 0) sprite.setX(0);
        if (sprite.getX() > worldWidth - sprite.getWidth())
            sprite.setX(worldWidth - sprite.getWidth());
    }

    public float getCenterX() {
        return sprite.getX() + sprite.getWidth() / 2;
    }

    public Sprite getSprite() {
        return sprite;
    }

    public void reset() {
        sprite.setPosition(200, groundY);
        isJumping = false;
        velocityY = 0;
    }

    public void draw(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {
        sprite.draw(batch);
    }

    public void dispose() {
        texture.dispose();
    }
}
