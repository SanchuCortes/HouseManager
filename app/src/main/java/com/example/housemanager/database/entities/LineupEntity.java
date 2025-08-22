package com.example.housemanager.database.entities;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "lineups")
public class LineupEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "userId")
    private int userId;

    @ColumnInfo(name = "matchday")
    private int matchday;

    // 11 huecos: ids de jugador, pueden ser nulos si no hay jugador asignado
    @Nullable @ColumnInfo(name = "slot1")  private Integer slot1;
    @Nullable @ColumnInfo(name = "slot2")  private Integer slot2;
    @Nullable @ColumnInfo(name = "slot3")  private Integer slot3;
    @Nullable @ColumnInfo(name = "slot4")  private Integer slot4;
    @Nullable @ColumnInfo(name = "slot5")  private Integer slot5;
    @Nullable @ColumnInfo(name = "slot6")  private Integer slot6;
    @Nullable @ColumnInfo(name = "slot7")  private Integer slot7;
    @Nullable @ColumnInfo(name = "slot8")  private Integer slot8;
    @Nullable @ColumnInfo(name = "slot9")  private Integer slot9;
    @Nullable @ColumnInfo(name = "slot10") private Integer slot10;
    @Nullable @ColumnInfo(name = "slot11") private Integer slot11;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getMatchday() { return matchday; }
    public void setMatchday(int matchday) { this.matchday = matchday; }

    public Integer getSlot1() { return slot1; }
    public void setSlot1(Integer slot1) { this.slot1 = slot1; }
    public Integer getSlot2() { return slot2; }
    public void setSlot2(Integer slot2) { this.slot2 = slot2; }
    public Integer getSlot3() { return slot3; }
    public void setSlot3(Integer slot3) { this.slot3 = slot3; }
    public Integer getSlot4() { return slot4; }
    public void setSlot4(Integer slot4) { this.slot4 = slot4; }
    public Integer getSlot5() { return slot5; }
    public void setSlot5(Integer slot5) { this.slot5 = slot5; }
    public Integer getSlot6() { return slot6; }
    public void setSlot6(Integer slot6) { this.slot6 = slot6; }
    public Integer getSlot7() { return slot7; }
    public void setSlot7(Integer slot7) { this.slot7 = slot7; }
    public Integer getSlot8() { return slot8; }
    public void setSlot8(Integer slot8) { this.slot8 = slot8; }
    public Integer getSlot9() { return slot9; }
    public void setSlot9(Integer slot9) { this.slot9 = slot9; }
    public Integer getSlot10() { return slot10; }
    public void setSlot10(Integer slot10) { this.slot10 = slot10; }
    public Integer getSlot11() { return slot11; }
    public void setSlot11(Integer slot11) { this.slot11 = slot11; }
}
