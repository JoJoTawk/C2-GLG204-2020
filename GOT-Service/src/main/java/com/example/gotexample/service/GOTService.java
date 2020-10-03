package com.example.gotexample.service;

import com.example.gotexample.Modules.GOTHeroes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GOTService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<GOTHeroes> getAllHeroes() {
    String sql = "Select * from GOT_HEROES";
    List<GOTHeroes> listOfHeroes = new ArrayList<>();
    jdbcTemplate.query(sql,(resultSet) -> {
        GOTHeroes hero = new GOTHeroes();
        hero.setId(resultSet.getInt("HERO_ID"));
        hero.setTitle(resultSet.getString("HERO_TITLE"));
        hero.setName(resultSet.getString("HERO_NAME"));
        hero.setAlliance(resultSet.getString("HERO_ALLEGIANCE"));
        hero.setImageURL(resultSet.getString("HERO_IMAGE_URL"));
        listOfHeroes.add(hero);
    });
        System.out.println("Heroes List : \n [" + listOfHeroes + "]");
    return listOfHeroes;
    }
}
