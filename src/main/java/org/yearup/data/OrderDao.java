package org.yearup.data;

import java.math.BigDecimal;
import java.sql.SQLException;

public interface OrderDao {
    void checkout( int  userId) throws Exception;
}
