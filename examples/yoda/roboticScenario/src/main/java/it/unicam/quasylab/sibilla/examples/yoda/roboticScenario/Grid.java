/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 *             Copyright (C) 2020.
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package it.unicam.quasylab.sibilla.examples.yoda.roboticScenario;

import it.unicam.quasylab.sibilla.core.models.yoda.YodaScene;
import it.unicam.quasylab.sibilla.core.models.yoda.YodaValue;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.LinkedList;

public class Grid implements YodaScene {

    private final int width;
    private final int height;
    private final LinkedList<Obstacle> obstacles;

    public Grid(int width, int height, LinkedList<Obstacle> obstacles){
        this.width = width;
        this.height = height;
        this.obstacles = obstacles;
    }



    @Override
    public int getWidthInt() {
        return width;
    }

    @Override
    public int getHeightInt() {
        return height;
    }

    LinkedList<Obstacle> getObstacles(){
        return obstacles;
    }

    @Override
    public void addObject(int posx, int posy) {
        obstacles.add(new Obstacle(posx, posy));
    }

    public int getNumberOfObstacles(){
        return obstacles.size();
    }

    @Override
    public boolean thereIsSomething(int posx, int posy) {
        if ((posx == -1) || (posx == width)){
            return true;
        }
        if ((posy == -1)) {
            return true;
        }
        return this.obstacles.stream().anyMatch(o -> o.isPlacedAt(posx, posy));
    }


    public static Grid generate(RandomGenerator rg, int width, int height, int numberOfObstacles) {
        LinkedList<Obstacle> obstacles = new LinkedList<>();
        boolean[][] coordinates = new boolean[width][height];
        int counter = 0;
        while (counter < numberOfObstacles){
            int x = 1+rg.nextInt(width-2);
            int y = 1+rg.nextInt(height-2);
            if (!coordinates[x][y]) {
                obstacles.add(new Obstacle(x,y));
                coordinates[x][y] = true;
                counter++;
            }
        }
        return new Grid(width, height, obstacles);
    }

    public static Grid generateThroughColumn(RandomGenerator rg, int width, int height, int numberOfObstacles) {
        LinkedList<Obstacle> obstacles = new LinkedList<>();
        boolean [][] coordinates = new boolean[width][height];
        int multi = 0;
        int counter = 0;
        while (counter < numberOfObstacles) {
            multi = counter /width;
            int x = counter-(width*multi);
            int y = 1+rg.nextInt(height-2);
            if (!coordinates[x][y]) {
                obstacles.add(new Obstacle(x, y));
                coordinates[x][y] = true;
                counter++;
            }
        }
        return new Grid(width, height, obstacles);
    }

    public static Grid generateDiagonal(RandomGenerator rg, int width, int height, int numberOfObstacles) {
        LinkedList<Obstacle> obstacles = new LinkedList<>();
        boolean[][] coordinates = new boolean[width][height];
        int counter = 0;
        boolean flag = true;
        while (counter < numberOfObstacles) {
            if (flag) {
                int rowStart = 1+ rg.nextInt(height-5);
                for (int j = 0; j<5; j++){
                    int x = counter;
                    int y = rowStart++;
                    if (!coordinates[x][y]) {
                        obstacles.add(new Obstacle(x, y));
                        coordinates[x][y] = true;
                        counter++;
                    }
                }
                flag = false;
            } else {
                int rowStart = height - rg.nextInt(height-5);
                for (int j = 0; j<5; j++) {
                    int x = counter;
                    int y = rowStart--;
                    if (!coordinates[x][y]) {
                        obstacles.add(new Obstacle(x, y));
                        coordinates[x][y] = true;
                        counter++;
                    }
                }
                flag = true;
            }

        }
        return new Grid(width, height, obstacles);
    }

    public static Grid generateCulDeSac(RandomGenerator rg, int width, int height) {
        LinkedList<Obstacle> obstacles = new LinkedList<>();
        int numberOfCulDeSac = width /10;
        for (int i = 0; i<numberOfCulDeSac; i++){
            int startingX = i*10 + rg.nextInt(7);
            int startingY = 1+ rg.nextInt(height-4);
            getCulDeSac(4,3,startingX, startingY,obstacles);
        }
        return new Grid(width, height, obstacles);
    }

    private static void getCulDeSac(int width, int height, int startingX, int startingY, LinkedList<Obstacle> obstacles) {
        for (int i = 0; i < height; i++) {
            int x = startingX;
            int y = startingY+i;
            obstacles.add(new Obstacle(x,y));
        }
        for (int i = 1; i < width; i++) {
            int x = startingX + i;
            int y = startingY+height;
            obstacles.add(new Obstacle(x,y));
        }
        for (int i = 0; i < height; i++){
            int x = startingX + width;
            int y = startingY+height-i;
            obstacles.add(new Obstacle(x,y));
        }

    }

    @Override
    public double getWidthReal() {
        System.err.println("Method not available");
        return 0;
    }


    @Override
    public double getHeightReal() {
        System.err.println("Method not available");
        return 0;
    }

    @Override
    public void addObject(double posx, double posy) {
        System.err.println("Method not available");
    }

    @Override
    public boolean thereIsSomething(double posx, double posy) {
        return thereIsSomething((int) posx, (int) posy);
    }

    @Override
    public boolean thereIsSomething(YodaValue... values) {
        System.err.println("Method not available");
        return false;
    }
}
