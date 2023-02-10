import edu.ucr.cs.riple.taint.ucrtainting.qual.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Test taint passing through library method invocation
class SQLTestInvocation {
    void testTainted(@RTainted String y) throws SQLException {
        try (Connection conn = DriverManager
                .getConnection("jdbc:mysql://localhost/test?serverTimezone=UTC",
                        "myUsername", "myPassword")) {
            // :: error: argument
            PreparedStatement selectStatement = conn.prepareStatement(y);
            ResultSet rs = selectStatement.executeQuery();
        }
    }
}
