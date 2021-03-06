package com.infiniteloop.ride;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.StretchViewport;

import java.util.ArrayList;


/**
 * Created by jackthebones on 12/06/15.
 */
public class GameplayState extends ScreenAdapter {

    public RideGame game;
    public OrthographicCamera camera;

    public static Stage GameplayStage;

    public ArrayList<Shot> ShotQueue = new ArrayList();

    private Ship ship;
    private SpaceBackground spacebackground;
    private Shot shot;
    private Alien alien;
    private Kamikaze kamikaze;
    private Bridge bridge;
    private Health health;
    private Coins coin;
    private Gas gas;

    public static Label label;

    private BitmapFont font;


    public GameplayState(RideGame game, int Aliens) throws InterruptedException {
        this.game = game;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, RideGame.WIDHT, RideGame.HEIGHT);

        GameplayStage = new Stage(new StretchViewport(RideGame.WIDHT, RideGame.HEIGHT));

        font = new BitmapFont();
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        label = new Label("Life : " + Ship.CurrentLife + "  " + "Gas : " + Ship.CurrentGas + "  "
                + "Score : " + Ship.CurrentScore, new Label.LabelStyle(font, Color.WHITE));
        label.setPosition(10,10);

        ship = new Ship();
        ship.setPosition(RideGame.WIDHT / 2, RideGame.HEIGHT / 2, Align.center);

        alien = new Alien(10);
        alien.setPosition(MathUtils.random(32, RideGame.WIDHT - 32), RideGame.HEIGHT, Align.center);

        kamikaze = new Kamikaze(MathUtils.random(3,10));
        kamikaze.setPosition(MathUtils.random(32, RideGame.WIDHT),
                RideGame.HEIGHT + MathUtils.random(700,1700), Align.center);

        bridge = new Bridge(MathUtils.random(1,5));
        bridge.setPosition(RideGame.WIDHT / 2, RideGame.HEIGHT + MathUtils.random(110,170), Align.center);

        coin = new Coins();
        coin.setPosition(MathUtils.random(32, RideGame.WIDHT),
                RideGame.HEIGHT + MathUtils.random(50,200), Align.center);

        health = new Health();
        health.setPosition(MathUtils.random(32, RideGame.WIDHT),
                RideGame.HEIGHT + MathUtils.random(500, 1000), Align.center);

        gas = new Gas();
        gas.setPosition(MathUtils.random(32, RideGame.WIDHT),
                RideGame.HEIGHT + MathUtils.random(500, 1000), Align.center);

        spacebackground = new SpaceBackground();
        spacebackground.setPosition(0,0);

        GameplayStage.addActor(spacebackground);
        GameplayStage.addActor(bridge);
        GameplayStage.addActor(ship);
        GameplayStage.addActor(alien);
        GameplayStage.addActor(kamikaze);
        GameplayStage.addActor(label);
        GameplayStage.addActor(coin);
        GameplayStage.addActor(gas);
        GameplayStage.addActor(health);

        InitInputProcessor();
    }

    public static void ResetVariables(int i){
        if(i == 1){
            Ship.CurrentGas = 100;
            Ship.CurrentLife = 100;
        }
        else{
            Ship.CurrentGas = 100;
            Ship.CurrentLife = 100;
            Ship.CurrentScore = 100;
        }

    }


    private void InitInputProcessor() {
        Gdx.input.setInputProcessor(new InputAdapter(){

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                //ship.Jump();
                shot = new Shot(200f);

                if (ShotQueue.size() >= 3){
                    return false;
                }else{

                    shot.setPosition(ship.getX(Align.center), ship.getY(Align.top), Align.bottom);
                    GameplayStage.addActor(shot);
                    ShotQueue.add(shot);
                return true;
                }
            }
        });
    }



    @Override
    //RENDER DE PANTALLA --- LOOP DE RENDERIZADO POR FPS.
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        GameplayStage.act();
        //Revisa si hay colisiones por cada vez que se refresca la pantalla.
        CheckIfGameOver();
        CheckIfStageCleared();
        CheckColisions();
        GameplayStage.draw();

        KeyMovementControls();
        AccelerometerMovementControls();
    }

    private void KeyMovementControls() {
        if(Gdx.input.isKeyPressed(Input.Keys.A)){
            ship.MoveLeft();
        }
        if(Gdx.input.isKeyPressed(Input.Keys.D)){
            ship.MoveRight();
        }
    }

    public void CheckIfStageCleared(){
        if(alien.ALIENAMOUNT == 0){
            Ship.CurrentGas = 100;
            Ship.CurrentLife = 100;
            GameplayStage.clear();
            GameplayStage.dispose();
            dispose();
            GraphState.UpdateMovement();
            game.setScreen(new GraphState(game));
        }
    }

    public void CheckIfGameOver(){
        if(Ship.state == Ship.State.dead){
            GameplayStage.clear();
            GameplayStage.dispose();
            ResetVariables(2);
            dispose();
            game.setScreen(new GameOverState(game));
        }
    }

    private void AccelerometerMovementControls(){
        float acceleration=Gdx.input.getAccelerometerX();

        if (Math.abs(acceleration) > 0.5f){
            if (acceleration < 0){
                ship.MoveRight();
            }
            else{
                ship.MoveLeft();
            }
        }
    }

    public void resize(int widht, int height){
        camera.setToOrtho(false, widht, height);
        //Assets.batch.setProjectionMatrix(camera.combined);
    }

    private void CheckColisions(){
        if(shot != null) {
            if(alien != null){
                if (!ShotQueue.isEmpty()) {
                    for (int i=0; i < ShotQueue.size(); i++) {
                        if (ShotQueue.get(i).getShotPerimeter().overlaps(alien.getAlienPerimeter())) {
                            ShotQueue.get(i).remove();
                            ShotQueue.get(i).clear();
                            ShotQueue.get(i).setShotPerimeter(new Rectangle(0, 0, -30, -30));
                            alien.HitTaken();
                            ShotQueue.remove(i);
                        }
                    }
                }
            }
        }
        if(Alien.alienShot != null){
            if(Alien.alienShot.getAlienShotPerimeter().overlaps(ship.getShipPerimeter())){
                Alien.alienShot.remove();
                Alien.alienShot.clear();
                Alien.alienShot.setAlienShotPerimeter(new Rectangle(0, 0, -30, -30));
                Ship.HitTaken(2);
            }
        }
        if(alien != null) {
            if(ship != null){
                if(alien.getAlienPerimeter().overlaps(ship.getShipPerimeter())){
                    ship.HitTaken(alien.AlienLifePoints * 2);
                    alien.state = Alien.State.dead;
                    alien.ALIENAMOUNT --;
                }
            }
        }
        if(shot != null) {
            if(kamikaze != null){
                if (!ShotQueue.isEmpty()){
                    for (int i=0; i < ShotQueue.size(); i++){
                        if(ShotQueue.get(i).getShotPerimeter().overlaps(kamikaze.getKamikazePerimeter())){
                            ShotQueue.get(i).remove();
                            ShotQueue.get(i).clear();
                            ShotQueue.get(i).setShotPerimeter(new Rectangle(0, 0, -30, -30));
                            kamikaze.HitTaken();
                            ShotQueue.remove(i);

                        }
                    }
                }
            }
        }
        if(kamikaze != null) {
            if(ship != null){
                if(kamikaze.getKamikazePerimeter().overlaps(ship.getShipPerimeter())){
                    ship.HitTaken((int)(ship.CurrentLife * 0.50));
                    kamikaze.state = Kamikaze.State.dead;
                    kamikaze.KAMIKAZEAMOUNT --;
                }
            }
        }
        if(bridge != null) {
            if(ship != null){
                if(bridge.getBridgePerimeter().overlaps(ship.getShipPerimeter())){
                    ship.HitTaken((int)(ship.CurrentLife * 0.35));
                    bridge.state = Bridge.State.dead;
                    bridge.BRIDGEAMOUNT --;
                }
            }
        }
        if(shot != null) {
            if(bridge != null){
                if (!ShotQueue.isEmpty()){
                    for (int i=0; i < ShotQueue.size(); i++){
                        if(ShotQueue.get(i).getShotPerimeter().overlaps(bridge.getBridgePerimeter())){
                            ShotQueue.get(i).remove();
                            ShotQueue.get(i).clear();
                            ShotQueue.get(i).setShotPerimeter(new Rectangle(0, 0, -30, -30));
                            bridge.HitTaken();
                            ShotQueue.remove(i);

                        }
                    }
                }
            }
        }
        if(coin != null) {
            if(ship != null){
                if(coin.getCoinPerimeter().overlaps(ship.getShipPerimeter())){
                    coin.HitTaken();
                }
            }
        }
        if(health != null) {
            if(ship != null){
                if(health.getHealthPerimeter().overlaps(ship.getShipPerimeter())){
                    health.HitTaken();
                }
            }
        }
        if(gas != null) {
            if(ship != null){
                if(gas.getGasPerimeter().overlaps(ship.getShipPerimeter())){
                    gas.HitTaken();
                }
            }
        }
        if (shot!= null){
            if (!ShotQueue.isEmpty()){
                for (int i = 0;i < ShotQueue.size();i++){
                    if (ShotQueue.get(i).IsAboveTop()){
                        ShotQueue.get(i).remove();
                        ShotQueue.get(i).clear();
                        ShotQueue.get(i).setShotPerimeter(new Rectangle(0, 0, -30, -30));
                        ShotQueue.remove(i);
                    }else{}
                }
            }
        }
    }
}
