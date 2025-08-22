package com.example.housemanager.api.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Alineación con listas de titulares y suplentes.
 */
public class Lineup {
    private List<LineupEntryAPI> startXI = new ArrayList<>();
    private List<LineupEntryAPI> substitutes = new ArrayList<>();

    public Lineup() {}

    public List<LineupEntryAPI> getStartXI() { return startXI; }
    public void setStartXI(List<LineupEntryAPI> startXI) { this.startXI = startXI != null ? startXI : new ArrayList<>(); }

    public List<LineupEntryAPI> getSubstitutes() { return substitutes; }
    public void setSubstitutes(List<LineupEntryAPI> substitutes) { this.substitutes = substitutes != null ? substitutes : new ArrayList<>(); }
}
