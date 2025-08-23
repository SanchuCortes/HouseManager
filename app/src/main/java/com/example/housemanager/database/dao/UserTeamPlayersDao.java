package com.example.housemanager.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.housemanager.database.entities.PlayerEntity;
import com.example.housemanager.database.entities.UserTeamPlayerEntity;

import java.util.List;

@Dao
public interface UserTeamPlayersDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void add(UserTeamPlayerEntity e);

    @Query("SELECT p.* FROM players p INNER JOIN user_team_players utp ON p.playerId = utp.playerId WHERE utp.leagueId=:leagueId AND utp.teamId=:teamId ORDER BY p.position ASC, p.name ASC")
    LiveData<List<PlayerEntity>> getSquad(int leagueId, int teamId);
}
