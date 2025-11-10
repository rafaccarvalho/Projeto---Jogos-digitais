package br.mackenzie.screens;

import br.mackenzie.Main;

public class GameScreen3 extends BaseGameScreen {
    public GameScreen3(Main game) { super(game); }

    @Override protected String getMapPath() { return "mapafase3.tmx"; }
    @Override protected BaseGameScreen newInstance() { return new GameScreen3(game); }
    @Override protected BaseGameScreen getNextScreen() { return null; } // última fase
}
