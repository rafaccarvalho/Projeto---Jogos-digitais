package br.mackenzie.screens;

import br.mackenzie.Main;

public class GameScreen2 extends BaseGameScreen {
    public GameScreen2(Main game) { super(game); }

    @Override protected String getMapPath() { return "mapafase2.tmx"; }
    @Override protected BaseGameScreen newInstance() { return new GameScreen2(game); }
    @Override protected BaseGameScreen getNextScreen() { return new GameScreen3(game); }
}
