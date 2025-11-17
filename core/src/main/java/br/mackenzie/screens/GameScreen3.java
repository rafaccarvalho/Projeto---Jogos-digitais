package br.mackenzie.screens;

import br.mackenzie.Main;
import com.badlogic.gdx.Screen;

public class GameScreen3 extends BaseGameScreen {

    public GameScreen3(Main game) {
        super(game);
    }

    @Override
    protected String getMapPath() {
        return "mapafase3.tmx"; // caminho do seu mapa
    }

    @Override
    protected BaseGameScreen newInstance() {
        return new GameScreen3(game);
    }

    // Última fase → envia para a FinalScreen
    @Override
    protected Screen getNextScreen() {
        return new FinalScreen(game);
    }

}
