/*
 * MindmapsDB - A Distributed Semantic Database
 * Copyright (C) 2016  Mindmaps Research Ltd
 *
 * MindmapsDB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MindmapsDB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MindmapsDB. If not, see <http://www.gnu.org/licenses/gpl.txt>.
 */

package io.mindmaps.migration.sql;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.stream.Collectors;

public class SQLMigratorUtil {

    public static final String USER = "test";
    public static final String PASS = "";
    public static final String DRIVER = "org.h2.Driver";
    public static final String URL = "jdbc:h2:~/test;";

    public static String readSql(String name) {
        try {
            return Files.readAllLines(new File(SQLMigratorUtil.class.getClassLoader().getResource(name).getPath()).toPath()).stream()
                    .filter(line -> !line.startsWith("--"))
                    .collect(Collectors.joining());
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    public static String readSqlSchema(String name){
        return readSql(name + "/create-db.sql");
    }

    public static String readSqlData(String name){
        return readSql(name + "/insert-data.sql");
    }

    public static Connection setupExample(String example) throws SQLException {

        // read schema and data from file
        String schema = readSqlSchema(example);
        String data = readSqlData(example);

        Connection connection;
        try {
            Class.forName(DRIVER).newInstance();
            connection = DriverManager.getConnection(URL, USER, PASS);
        }
        catch (SQLException|ClassNotFoundException|InstantiationException|IllegalAccessException e){
            throw new RuntimeException(e);
        }

        // attempt to clear DB
        try { connection.prepareStatement("DROP ALL OBJECTS").execute(); }
        catch (SQLException e){
            System.out.println("Was not able to drop all objects due to DB type");
        }

        // load schema and data into db
        connection.prepareStatement(schema).execute();
        connection.prepareStatement(data).execute();

        return connection;
    }

}
