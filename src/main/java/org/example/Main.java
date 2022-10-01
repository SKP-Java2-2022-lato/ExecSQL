package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Properties;
import java.util.Scanner;

/**
 * Program pozwala wykonać dowolny kod SQL
 * Pod warunkiem że instrukcje rozdzielone są znakiem nowej lini
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    public static void main(String[] args) {
      String path;

      while(true){
          System.out.print("Podaj ścieżkę do pliku lub exit aby zamknąć program: ");
          path = scanner.nextLine();
          if(path.equalsIgnoreCase("exit"))
              break;
          try(Scanner file = new Scanner(Paths.get(path), StandardCharsets.UTF_8);
              Connection connection = getConnection();
              Statement statement =connection.createStatement()){

              while (file.hasNextLine()){
                  String line = file.nextLine();
                  // usuwam srednik
                  if(line.endsWith(";"))
                      line = line.substring(0, line.length()-1);

                  boolean isResult = statement.execute(line);
                  if(isResult){
                      ResultSet resultSet = statement.getResultSet();
                      showResultSet(resultSet);
                      resultSet.close();
                  }
              }

          }catch (IOException e){
              e.printStackTrace();
          }catch (SQLException sqlException){
              for(Throwable t: sqlException)
                  t.printStackTrace();
          }
      }

    }

    /**
     * Wyświetla wyniki zapytania
     * @param resultSet zbiór wyników do wyświetlena
     */
    private static void showResultSet(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        for(int i=1; i<=columnCount; i++){
            if(i>1)
                System.out.print(", ");
            System.out.print(metaData.getColumnLabel(i));
        }
        System.out.println();

        while(resultSet.next()){
            for(int i=1; i<=columnCount; i++){
                if(i>1)
                    System.out.print(", ");
                System.out.print(resultSet.getString(i));
            }
            System.out.println();
        }
    }

    /**
     * Ustanawia połączenie z bazą danych
     * @return obiekt klasy Connection
     * @throws IOException kiedy nie znaleziono pliku database.properties
     * @throws SQLException kiedy nie ustanowiono połączenia
     */

    private static Connection getConnection() throws IOException, SQLException {
        Properties properties = new Properties();
        try(InputStream in = Files.newInputStream(Paths.get("database.properties"))){
            properties.load(in);
        }
        String drivers = properties.getProperty("jdbc.drivers");
        if(drivers != null) System.setProperty("jdbc.drivers", drivers);
        String url = properties.getProperty("jdbc.url");
        String username = properties.getProperty("jdbc.username");
        String password = properties.getProperty("jdbc.password");

        return DriverManager.getConnection(url, username, password);
    }
}