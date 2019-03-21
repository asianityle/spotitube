package com.spotitube.dao;

import com.spotitube.Token;
import com.spotitube.database.DatabaseRequest;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class TokenDAO {

    @Inject
    private DatabaseRequest request;

    public Token createTokenForUser(String username){
        Token token;

        try(
            Connection connection = request.connectToDB();
            PreparedStatement st = connection.prepareStatement("INSERT INTO TOKEN VALUES (?,?,?)");
        ){
            LocalDateTime dt = LocalDateTime.now().plusHours(24);
            Date date = Date.from(dt.atZone( ZoneId.systemDefault()).toInstant());
            String userToken = UUID.randomUUID().toString();
            st.setString(1,username);
            st.setString(2,userToken);
            st.setDate(3, convertUtilToSql(date));
            st.executeQuery();

            token = new Token(username,userToken);
        }
        catch(SQLException e){
            throw new RuntimeException(e);
        }
        return token;
    }


    private static java.sql.Date convertUtilToSql(java.util.Date uDate) {
        java.sql.Date sDate = new java.sql.Date(uDate.getTime());
        return sDate;
    }

    public boolean isTokenValid(Token token){
        boolean isValid = false;
        ResultSet resultSet = null;

        try{
            Connection connection = request.connectToDB();
            PreparedStatement st = connection.prepareStatement("SELECT * FROM TOKEN WHERE U_NAME = ? AND U_TOKEN = ?");
            st.setString(1, token.getUser());
            st.setString(2,token.getToken());
            resultSet = st.executeQuery();

            LocalDateTime now = LocalDateTime.now();

            while(resultSet.next()){
                Date exDate = resultSet.getDate("EXPIRY_DATE");
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String strDate = dateFormat.format(exDate);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime expiryDate = LocalDateTime.parse(strDate,formatter);
                if(expiryDate.isAfter(now)){
                    isValid = true;
                }
            }
        }
        catch(SQLException e){
            System.out.println("Error" + e);
        }
        return isValid;
    }

    public Token getToken(String token){
        Token verkrijgToken = null;
        ResultSet resultSet = null;
        try{
            Connection connection = request.connectToDB();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM TOKEN WHERE U_TOKEN = ?");
            preparedStatement.setString(1,token);
            resultSet = preparedStatement.executeQuery();

            while(resultSet.next()){
                String dbToken = resultSet.getString("U_TOKEN");
                String user = resultSet.getString("U_NAME");
                verkrijgToken = new Token(user,dbToken);
            }
        }
        catch(SQLException e){
            System.out.println("Error" + e);
        }
        return verkrijgToken;
    }
}