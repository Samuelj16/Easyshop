package org.yearup.data;


import org.yearup.models.Profile;

import java.sql.SQLException;

public interface ProfileDao
{
    Profile create(Profile profile);
    void updateProfile(int id,Profile profile) throws SQLException;
    Profile getByUserId(int userId) throws SQLException;
}
