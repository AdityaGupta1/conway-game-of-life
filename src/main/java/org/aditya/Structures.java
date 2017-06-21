package org.aditya;

import java.util.*;

class Structures {
    private boolean[][] glider1 = padArray(new boolean[][]{
            {false, true, false},
            {false, false, true},
            {true, true, true}
    });

    private boolean[][] glider2 = padArray(new boolean[][]{
            {true, false, true},
            {false, true, true},
            {false, true, false}
    });

    private List<Object> gliders = new ArrayList<>();

    private List<List<Object>> structures = new ArrayList<>();

    void addStructuresToLists() {
        gliders.add("gliders");
        gliders.add(glider1);
        gliders.add(glider2);

        structures.add(gliders);
    }

    private boolean[][] padArray(boolean[][] array) {
        boolean[][] paddedArray = new boolean[array.length + 2][array[0].length + 2];
        for (boolean[] line : paddedArray) {
            Arrays.fill(line, false);
        }

        for (int i = 0; i < array.length; i++) {
            System.arraycopy(array[i], 0, paddedArray[i + 1], 1, array[0].length);
        }

        return paddedArray;
    }

    private boolean[][] rotateArray(boolean[][] array, int times) {
        if (times == 0) {
            return array;
        } else {
            boolean[][] rotatedArray = new boolean[array[0].length][array.length];
            for (int i = 0; i < array[0].length; i++) {
                for (int j = array.length - 1; j >= 0; j--) {
                    rotatedArray[i][j] = array[j][i];
                }
            }

            return rotateArray(rotatedArray, times - 1);
        }
    }

    private List<int[]> findIn2DArray(boolean[][] array, boolean[][] subArray) {
        List<int[]> indices = new ArrayList<>();

        for (int x = 0; x < array.length - subArray.length + 1; ++x)
            loopY:for (int y = 0; y < array[x].length - subArray[0].length + 1; ++y) {
                for (int xx = 0; xx < subArray.length; ++xx)
                    for (int yy = 0; yy < subArray[0].length; ++yy) {
                        if (array[x + xx][y + yy] != subArray[xx][yy]) {
                            continue loopY;
                        }
                    }

                indices.add(new int[]{x, y});
            }

        return indices;
    }

    Map<String, List<Structure>> findStructures(boolean[][] array) {
        Map<String, List<Structure>> allIndices = new HashMap<>();

        // `structureForms` example: `gliders`
        for (List<Object> structureForms : structures) {
            String name = "";
            List<Structure> indices = new ArrayList<>();

            int xSize = ((boolean[][]) structureForms.get(1)).length;
            int ySize = ((boolean[][]) structureForms.get(1))[0].length;

            // `structure`s in `gliders`: "gliders", boolean[][] glider1, boolean[][] glider2
            for (Object structure : structureForms) {
                if (structure instanceof String) {
                    name = String.valueOf(structure);
                } else {
                    for (int i = 0; i < 3; i++) {
                        List<Structure> thisRotationStructures = new ArrayList<>();
                        for (int[] index : findIn2DArray(array, rotateArray((boolean[][]) structure, i))) {
                            boolean isRotationEven = i % 2 == 2;
                            thisRotationStructures.add(new Structure(index, new int[]{isRotationEven ? xSize : ySize, isRotationEven ? ySize : xSize}));
                        }
                        indices.addAll(thisRotationStructures);
                    }
                }
            }

            allIndices.put(name, indices);
        }

        return allIndices;
    }
}
