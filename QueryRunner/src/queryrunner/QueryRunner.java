/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package queryrunner;

import javax.management.Query;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/**
 * QueryRunner takes a list of Queries that are initialized in it's constructor
 * and provides functions that will call the various functions in the QueryJDBC class
 * which will enable MYSQL queries to be executed. It also has functions to provide the
 * returned data from the Queries. Currently the eventHandlers in QueryFrame call these
 * functions in order to run the Queries.
 */
public class QueryRunner {


    public QueryRunner() {
        this.m_jdbcData = new QueryJDBC();
        m_updateAmount = 0;
        m_queryArray = new ArrayList<>();
        m_error = "";
        m_querynames = new String[12];
        m_queryParamNames = new String[12];

        // Project Application name
        this.m_projectTeamApplication = "MyRaceSignUp.com";

        //************************
        //***QUERY DECLARATIONS***
        //************************

        /**
         * Query #1 -- Revenue per event (query1)
         */
        m_queryArray.add(new QueryData("" +
                "SELECT E.EventName AS \"Event Name\", E.EventDate AS \"Date\", E.Capacity \"Max Capacity\", E.Occupied \"Registrants\", REV.Revenue\n" +
                "FROM `Event` AS E JOIN\n" +
                "\t(SELECT E.EventID, SUM(O.PayAmount) AS Revenue\n" +
                "\t FROM `Event` AS E JOIN `Order` AS O ON E.EventID = O.EventID\n" +
                "\t GROUP BY E.EventID) AS REV\n" +
                "ON E.EventID = REV.EventID ORDER BY E.EventName;", null, null, false, false));
        m_querynames[0] = "Revenue per Event";

        /**
         * Query #2 -- Player Total Fees (query2)
         */
        m_queryArray.add(new QueryData("SELECT \n" +
                "\tP.PlayerFirstname AS \"First Name\", \n" +
                "\tP.PlayerLastname AS \"Last Name\", \n" +
                "\tF.Fees AS \"Total Player Fees\"\n" +
                "FROM Player AS P \n" +
                "JOIN\n" +
                "\t(SELECT P.PlayerID, SUM(O.PayAmount) AS Fees\n" +
                "\t FROM Player AS P JOIN `Order` AS O ON P.PlayerID = O.PlayerID\n" +
                "\t GROUP BY P.PlayerID) AS F\n" +
                "ON P.PlayerID = F.PlayerID\n" +
                "ORDER BY P.PlayerFirstname, P.PlayerLastname;", null, null, false, false));   // THIS NEEDS TO CHANGE FOR YOUR APPLICATION
        m_querynames[1] = "Player Total Fees";


        /**
         * Query #3 -- Loss/Gain by OrganizerID - Parameter Query (query9)
         */
        m_queryArray.add(new QueryData("" +
                "SELECT EventID AS \"EventID\", EName AS \"Event Name\", Org AS \"Organizer\", LossOrGain AS \"Loss Or Gain\"\n" +
                "FROM (SELECT E.EventID, E.EventName AS EName, EO.OrganizerName AS Org, EO.EventOrganizerID as OrgID, (InnerQuary.TotalRev- E.EventCost) AS LossOrGain\n" +
                "FROM `Event` E INNER JOIN EventOrganizer AS EO ON E.EventOrganizerID = EO.EventOrganizerID\n" +
                "INNER JOIN (SELECT E.EventID, SUM(PayAmount) AS TotalRev\n" +
                "FROM `Order` O INNER JOIN `Event` E ON E.EventID = O.EventID GROUP BY E.EventID) InnerQuary\n" +
                "ON InnerQuary.EventID = E.EventID WHERE EO.EventOrganizerID = ?\n" +
                "ORDER BY LossOrGain DESC) AS L_OR_G;", new String[]{"Organizer ID"}, new boolean[]{false}, false, true));
        m_querynames[2] = "Loss/Gain by OrganizerID";

        /**
         * Query #4 -- 10 Popular States for Events(query7)
         */
        m_queryArray.add(new QueryData("" +
                "SELECT \n" +
                "\tInnerQuery.State AS 'Popular States to Hold Events In (Top Ten)'\n" +
                "FROM (\n" +
                "\tSELECT L.State, COUNT(E.LocationID) AS EventsPerState\n" +
                "\tFROM Event E\n" +
                "\tINNER JOIN Location L ON L.LocationID = E.LocationID\n" +
                "\tGROUP BY L.State\n" +
                "\tORDER BY EventsPerState DESC\n" +
                "\tLIMIT 10) InnerQuery;", null, null, false, false));
        m_querynames[3] = "10 Popular States for Events";

        /**
         * Query #5 -- 10 Popular States for Players(query 8)
         */
        m_queryArray.add(new QueryData("" +
                "SELECT InnerQuery.State AS '10 Active States'\n" +
                "FROM (SELECT P.State, COUNT(PlayerID) AS UsersPerState\n" +
                "FROM Player P GROUP BY P.State ORDER BY UsersPerState DESC LIMIT 10) InnerQuery;", null, null, false, false));
        m_querynames[4] = "Top 10 Player States";

        /**
         * Query #6 -- Best Race Performances (query5)
         */
        m_queryArray.add(new QueryData("" +
                "SELECT\n" +
                "\tE.EventID,\n" +
                "\tE.EventName \"Event\",\n" +
                "    E.EventDate \"Date\",\n" +
                "    P.PlayerID,\n" +
                "    P.PlayerFirstname 'First Name',\n" +
                "    P.PlayerLastname 'Last Name',\n" +
                "    D.DistanceValue AS \"Distance(m)\",\n" +
                "    S.SportsType As \"Sports\",\n" +
                "    R.ResultTime AS \"Time\",\n" +
                "    DATE_FORMAT(FROM_DAYS(DATEDIFF(now(),P.DateOfBirth)), '%Y')+0 AS Age,\n" +
                "    AVERAGE.KM_PACE \"Average pace in min/km\",\n" +
                "    AVERAGE.MI_PACE \"average pace in min/mi\"\n" +
                "FROM\n" +
                "\tPlayer AS P JOIN PlayerResult AS R ON P.PlayerID = R.PlayerID\n" +
                "    JOIN `Event` AS E ON R.EventID = E.EventID\n" +
                "    JOIN Event_has_Distance AS H ON E.EventID = H.EventID\n" +
                "    JOIN Distance AS D on H.DistanceID = D.DistanceID\n" +
                "    JOIN Sport S ON E.SportID = S.SportID\n" +
                "    LEFT JOIN\n" +
                "    (SELECT \n" +
                "\t\tP.PlayerID,\n" +
                "\t\tE.EventID,\n" +
                "\t\tCONCAT(FLOOR((TIME_TO_SEC(PR.ResultTime) / 60) / (D.DistanceValue / 1000)), \":\",\n" +
                "        LPAD(FLOOR(((TIME_TO_SEC(PR.ResultTime) / 60) / (D.DistanceValue / 1000) % 1) * 60), 2, '0')) AS KM_PACE,\n" +
                "\t    CONCAT(FLOOR((TIME_TO_SEC(PR.ResultTime) / 60) / ((D.DistanceValue / 1000) * 0.621371)), \":\",\n" +
                "        LPAD(FLOOR(((TIME_TO_SEC(PR.ResultTime) / 60) / ((D.DistanceValue / 1000) * 0.621371) % 1) * 60), 2, '0')) AS MI_PACE\n" +
                "\tFROM\n" +
                "\t\tPlayer AS P JOIN PlayerResult AS PR ON P.PlayerID = PR.PlayerID\n" +
                "\t\tJOIN `Event` AS E ON PR.EventID = E.EventID\n" +
                "\t\tJOIN Event_has_Distance AS ED ON E.EventID = ED.EventID\n" +
                "\t\tJOIN Distance AS D ON ED.DistanceID = D.DistanceID) AS AVERAGE ON AVERAGE.PlayerID = P.PlayerID AND AVERAGE.EventID = E.EventID\n" +
                "WHERE  ResultRank = 1\n" +
                "ORDER BY S.SportsType,D.DistanceValue, E.EventName,  R.ResultRank;", null, null, false, false));
        m_querynames[5] = "Best Race Performances";

        /**
         * Query #7 --Sports Overview (query4)
         */
        m_queryArray.add(new QueryData("" +
                "SELECT P1.SportsType, P2.Total_number \"NumPLayers\", P1.Total_number AS 'NumOrganizers', \n" +
                "AMOUNT.TOTALAMOUNT AS \"Revevue\", COST.TOTALCOST AS 'Sum Fees'\n" +
                "FROM (SELECT Count(S.SportID) Total_number, S.SportsType\n" +
                "FROM EventOrganizer EO JOIN Event E ON E.EventOrganizerID = EO.EventOrganizerID\n" +
                "JOIN Sport S ON S.SportID = E.SportID GROUP BY S.SportID) \n" +
                "AS P1 JOIN (SELECT Count(S.SportID) Total_number,S.SportsType\n" +
                "FROM PlayerResult R JOIN Event E ON R.EventID = E.EventID \n" +
                "JOIN Sport S ON S.SportID = E.SportID GROUP BY S.SportID\n" +
                "ORDER BY Count(S.SportID) DESC) AS P2 ON P1.SportsType = P2.SportsType\n" +
                "JOIN (SELECT SUM(O.PayAmount) AS TOTALAMOUNT,SportsType\n" +
                "FROM  `Event` E JOIN Sport S ON S.SportID = E.SportID \n" +
                "JOIN `Order` O ON  E.EventID = O.EventID\n" +
                "GROUP BY S.SportID) AS AMOUNT ON P1.SportsType = AMOUNT.SportsType\n" +
                "JOIN(SELECT SUM(E.EventCost) AS TOTALCOST,S.SportsType\n" +
                "FROM Event E JOIN Sport S ON E.SportID = S.SportID GROUP BY E.SportID) \n" +
                "AS COST ON COST.SportsType = AMOUNT.SportsType\n" +
                "WHERE P1.SportsType = P2.SportsType\n" +
                "ORDER BY AMOUNT.TOTALAMOUNT DESC, P1.Total_number DESC, P2.Total_number DESC;", null, null, false, false));
        m_querynames[6] = "Sports Overview";


        /**
         * Query #8  -- Player Data after X Birth Year
         */
        m_queryArray.add(new QueryData("" +
                "SELECT DISTINCT P.DateOfBirth AS \"DOB\", DATE_FORMAT(FROM_DAYS(DATEDIFF(now(),P.DateOfBirth)), '%Y')+0 AS Age,\n" +
                "P.PlayerFirstName AS \"FirstName\", P.PlayerLastName AS \"LastName\", P.State, P.ZipCode \n" +
                "FROM Player AS P JOIN PlayerResult AS PR ON P.PlayerID = PR.PlayerID\n" +
                "WHERE YEAR(P.DateOfBirth) >= ? AND EXISTS (SELECT PlayerID\n" +
                "FROM PlayerResult) ORDER BY P.DateOfBirth DESC, P.State ASC;", new String[]{"Year"}, new boolean[]{false}, false, true));
        m_querynames[7] = "Players born after X year";

        /**
         * Query #9 -- Results of Players born after 2000, parameter: sport type (query6)
         */
        m_queryArray.add(new QueryData("" +
                "SELECT\n" +
                "\tE.EventName \"Event\",\n" +
                "    P.PlayerFirstname FirstName,\n" +
                "    P.PlayerLastname LastName,\n" +
                "    P.DateOfBirth,\n" +
                "    D.DistanceValue AS \"Distance\",\n" +
                "    R.ResultRank AS \"Rank\",\n" +
                "    R.ResultTime AS \"Time\",\n" +
                "    AVERAGE.KM_PACE \"Average pace in min/km\",\n" +
                "    AVERAGE.MI_PACE \"Average pace in min/mi\"\n" +
                "FROM\n" +
                "\tPlayer AS P JOIN PlayerResult AS R ON P.PlayerID = R.PlayerID\n" +
                "    JOIN `Event` AS E ON R.EventID = E.EventID\n" +
                "    JOIN Event_has_Distance AS H ON E.EventID = H.EventID\n" +
                "    JOIN Distance AS D on H.DistanceID = D.DistanceID\n" +
                "    JOIN Sport S ON E.SportID = S.SportID\n" +
                "    LEFT JOIN\n" +
                "    (SELECT \n" +
                "\t\tP.PlayerID,\n" +
                "\t\tE.EventID,\n" +
                "\t\tCONCAT(FLOOR((TIME_TO_SEC(PR.ResultTime) / 60) / (D.DistanceValue / 1000)), \":\",\n" +
                "        LPAD(FLOOR(((TIME_TO_SEC(PR.ResultTime) / 60) / (D.DistanceValue / 1000) % 1) * 60), 2, '0')) AS KM_PACE,\n" +
                "\t    CONCAT(FLOOR((TIME_TO_SEC(PR.ResultTime) / 60) / ((D.DistanceValue / 1000) * 0.621371)), \":\",\n" +
                "        LPAD(FLOOR(((TIME_TO_SEC(PR.ResultTime) / 60) / ((D.DistanceValue / 1000) * 0.621371) % 1) * 60), 2, '0')) AS MI_PACE\n" +
                "\tFROM\n" +
                "\t\tPlayer AS P JOIN PlayerResult AS PR ON P.PlayerID = PR.PlayerID\n" +
                "\t\tJOIN `Event` AS E ON PR.EventID = E.EventID\n" +
                "\t\tJOIN Event_has_Distance AS ED ON E.EventID = ED.EventID\n" +
                "\t\tJOIN Distance AS D ON ED.DistanceID = D.DistanceID) AS AVERAGE ON AVERAGE.PlayerID = P.PlayerID AND AVERAGE.EventID = E.EventID\n" +
                "WHERE SportsType = ? AND P.DateOfBirth > '2000-01-01'\n" +
                "ORDER BY D.DistanceValue, E.EventName,  R.ResultRank;", new String[]{"SportType"}, new boolean[]{false}, false, true));
        m_querynames[8] = "Players born after 2000's result with X sport type";

        /**
         * Query #10  Events in Washington state that take place between input dates. -- parameter
         */
        m_queryArray.add(new QueryData("" + "SELECT E.EventName AS EventName, \n" +
                "\tS.SportsType AS Sport, \n" +
                "\tD.DistanceValue AS 'Distance(m)', \n" +
                "\tE.Difficulty AS Difficulty, \n" +
                "\tCAST(E.EventDate AS DATE) AS Date, \n" +
                "\tL.ZipCode AS Zip\n" +
                "FROM Event AS E\n" +
                "\tJOIN Location AS L\n" +
                "\t\tON E.LocationID = L.LocationID\n" +
                "\tJOIN Event_has_Distance AS EHD\n" +
                "\t\tON E.EventID = EHD.EventID\n" +
                "\tJOIN Distance AS D\n" +
                "\t\tON EHD.DistanceID = D.DistanceID\n" +
                "\tJOIN Sport AS S\n" +
                "\t\tON E.SportID = S.SportID\n" +
                "WHERE E.EventDate BETWEEN ? AND ?\n" +
                "ORDER BY E.EventDate, S.SportsType, E.Difficulty, E.EventName ASC;", new String[]{"Date From", "Date TO"}, new boolean[]{false, false}, false, true));
        m_querynames[9] = "Events in Washington state";

        /**
         * Query #11 - Add new Sport -- parameter
         */
        m_queryArray.add(new QueryData("" +
                "INSERT INTO Sport (SportsType) VALUES(?);" , new String[] {"New sport"}, new boolean[]{false}, true, true));
        m_querynames[10] = "Add new sport!";

        /**
         * Query #12 - View Sports
         */
        m_queryArray.add(new QueryData("Select * from Sport;", null, null, false, false));
        m_querynames[11] = "View All Sports";
    }


    public int GetTotalQueries() {
        return m_queryArray.size();
    }

    public int GetParameterAmtForQuery(int queryChoice) {
        QueryData e = m_queryArray.get(queryChoice);
        return e.GetParmAmount();
    }

    public String GetParamText(int queryChoice, int parmnum) {
        QueryData e = m_queryArray.get(queryChoice);
        return e.GetParamText(parmnum);
    }

    public String GetQueryText(int queryChoice) {
        QueryData e = m_queryArray.get(queryChoice);
        return e.GetQueryString();
    }

    /**
     * Function will return how many rows were updated as a result
     * of the update query
     *
     * @return Returns how many rows were updated
     */

    public int GetUpdateAmount() {
        return m_updateAmount;
    }

    /**
     * Function will return ALL of the Column Headers from the query
     *
     * @return Returns array of column headers
     */
    public String[] GetQueryHeaders() {
        return m_jdbcData.GetHeaders();
    }

    /**
     * After the query has been run, all of the data has been captured into
     * a multi-dimensional string array which contains all the row's. For each
     * row it also has all the column data. It is in string format
     *
     * @return multi-dimensional array of String data based on the resultset
     * from the query
     */
    public String[][] GetQueryData() {
        return m_jdbcData.GetData();
    }

    public String GetProjectTeamApplication() {
        return m_projectTeamApplication;
    }

    public boolean isActionQuery(int queryChoice) {
        QueryData e = m_queryArray.get(queryChoice);
        return e.IsQueryAction();
    }

    public boolean isParameterQuery(int queryChoice) {
        QueryData e = m_queryArray.get(queryChoice);
        return e.IsQueryParm();
    }


    public boolean ExecuteQuery(int queryChoice, String[] parms) {
        boolean bOK = true;
        QueryData e = m_queryArray.get(queryChoice);
        bOK = m_jdbcData.ExecuteQuery(e.GetQueryString(), parms, e.GetAllLikeParams());
        return bOK;
    }

    public boolean ExecuteUpdate(int queryChoice, String[] parms) {
        boolean bOK = true;
        QueryData e = m_queryArray.get(queryChoice);
        bOK = m_jdbcData.ExecuteUpdate(e.GetQueryString(), parms);
        m_updateAmount = m_jdbcData.GetUpdateCount();
        return bOK;
    }


    public boolean Connect(String szHost, String szUser, String szPass, String szDatabase) {

        boolean bConnect = m_jdbcData.ConnectToDatabase(szHost, szUser, szPass, szDatabase);
        if (bConnect == false)
            m_error = m_jdbcData.GetError();
        return bConnect;
    }

    public boolean Disconnect() {
        // Disconnect the JDBCData Object
        boolean bConnect = m_jdbcData.CloseDatabase();
        if (bConnect == false)
            m_error = m_jdbcData.GetError();
        return true;
    }

    public String GetError() {
        return m_error;
    }

    /**
     * gets a copy of the queryname array from the QueryRunner object
     *
     * @return string array copy
     */
    public String[] getNameArray() {
        String[] copy = new String[12];
        for (int i = 0; i < 12; i++) {
            copy[i] = m_querynames[i];
        }

        return copy;
    }

    /**
     * gets a copy of the paramName array from the QueryRunner object
     *
     * @return string array copy
     */
    public String[] getParamArray() {
        String[] copy = new String[12];
        for (int i = 0; i < 12; i++) {
            copy[i] = m_queryParamNames[i];
        }

        return copy;
    }

    private QueryJDBC m_jdbcData;
    private String m_error;
    private String m_projectTeamApplication;
    private ArrayList<QueryData> m_queryArray;
    private int m_updateAmount;
    private String[] m_querynames;  // String array with query names
    private String[] m_queryParamNames;  // String array with param names


    /**
     * Main method. To run application in console mode, create separate
     * configuration with "-console" placed in the program arguments.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        final QueryRunner queryrunner = new QueryRunner();

        //************************
        //****GUI APPLICATION*****
        //************************
        //GUI IMPROVEMENT:
        //1. Changed background color
        //2. Added background image
        //3. Changed name text color
        //4. The query drag name changed.
        if (args.length == 0) {
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    new QueryFrame(queryrunner).setVisible(true);
                }
            });
        }
        //************************
        //**CONSOLE APPLICATION***
        //************************
        else {

            if (args[0].equals("-console")) {
                int choice;  // User input value
                boolean connected;  // Successful connection to the DB
                String cont = " ";
                String szHost;  // Host values of the DB
                String szUser;  // Username of the DB
                String szPass;  // Password of the DB
                String szDatabase;  // DB name

                // NOTE - IF THERE ARE ANY ERRORS, please print the Error output
                // Welcome the user to the program.
                welcome();
                Scanner scanner = new Scanner(System.in);

                cont = " ";
                szHost = "cs100";
                szUser = "mm_cpsc502101team02";
                szPass = "mm_cpsc502101team02Pass-";
                szDatabase = "mm_cpsc502101team02";

                // Get the user credentials to access the database
//                System.out.print("Enter the Hostname: ");
//                szHost = scanner.nextLine();
//                System.out.print("Enter the Username: ");
//                szUser = scanner.nextLine();
//                System.out.print("Enter the Password: ");
//                szPass = scanner.nextLine();
//                System.out.print("Enter the Database: ");
//                szDatabase = scanner.nextLine();

                // Connect to the database, re-prompt if incorrect
                connected = queryrunner.Connect(szHost, szUser, szPass, szDatabase);
                do {
                    System.out.print("Enter the Hostname: ");
                    szHost = scanner.nextLine();
                    System.out.print("Enter the Username: ");
                    szUser = scanner.nextLine();
                    System.out.print("Enter the Password: ");
                    szPass = scanner.nextLine();
                    System.out.print("Enter the Database: ");
                    szDatabase = scanner.nextLine();
                    // Connect to the database
                    connected = queryrunner.Connect(szHost, szUser, szPass, szDatabase);
                    if (connected) {
                        System.out.println("Connected!");
                    } else{
                        System.out.println("Input has error, please try again!");
                    }
                }while(!connected);


                // Enter the main menu until user quits
                do {
                    // Print the menu choice screen, until the user selects quit
                    printMainMenu();

                    // Get the choice from the user
                    choice = getChoice();

                    // Validate the user input
                    while (choice > 2 || choice < 1) {
                        System.out.println("Invalid number! Please enter again: ");
                        choice = getChoice();
                    }

                    // Enter the query section
                    if (choice == 2) {
                        // Enter query screen
                        enterQueryScreen(queryrunner);
                    }
                } while (choice != 1);

                // Disconnect from the database
                queryrunner.Disconnect();
            }
        }
    }

    /**
     * Core logic of the console application that handles user input, accesses
     * and runs the various queries and handles the sorting of parameters
     *
     * @param queryrunner QueryRunner object
     */
    public static void enterQueryScreen(QueryRunner queryrunner) {
        String[] colHeaders;  // column names
        String[][] queryData;  // query result set
        String[] qNames = queryrunner.getNameArray();
        String[] pNames = queryrunner.getParamArray();
        String paramInput;  // Input taken from the user
        int numQueries = queryrunner.GetTotalQueries();
        int paramAmnt;  // Number of parameters in the query
        int choice;  // Choice of the query to run from the user
        int queryChoice;  // index of query chosen
        int rowsAffected;  // Number of rows returned from the query
        boolean nullValReturn;
        boolean bOk = true;  // boolean val to confirm successful JDBC actions
        Scanner sc = new Scanner(System.in);

        // Prompt the user for queries until satisfied
        do {
            String[] parmString = {};  // array of parameters

            // Print the query menu choices
            printQueryMenu(qNames);

            // Get the choice from the user
            choice = getChoice();

            // Confirm the number is valid
            while (choice > (numQueries + 1) || choice < 1) {
                System.out.println("Invalid number! Please enter again: ");
                choice = getChoice();
            }
            // Go into query parsing if not equal to quit value
            if (choice <= (numQueries + 1) && choice > 1) {
                // Get the choice from the user (choice = choice - 2)
                queryChoice = choice - 2;

                // Determine if query has parameters, get params if true
                if (queryrunner.isParameterQuery(queryChoice)) {
                    paramAmnt = queryrunner.GetParameterAmtForQuery(queryChoice);
                    parmString = new String[paramAmnt];

                    // Print the parameters to gather
                    for (int i = 0; i < paramAmnt; i++) {
                        System.out.print(queryrunner.GetParamText(queryChoice, i));
                        System.out.print(": ");
                        paramInput = sc.nextLine();

                        // Add the user input to the param string to execute
                        parmString[i] = paramInput;
                    }
                    System.out.println();
                }

                // If an action query, execute the action
                if (queryrunner.isActionQuery(queryChoice)) {
                    bOk = queryrunner.ExecuteUpdate(queryChoice, parmString);
                    if (bOk) {
                        rowsAffected = queryrunner.GetUpdateAmount();
                        System.out.println(rowsAffected + " row added!");
                        System.out.println();
                    } else {
                        System.out.println(queryrunner.GetError() + "\n");
                    }
                }
                // Otherwise, it is a parameter query
                else {
                    String line = "";
                    bOk = queryrunner.ExecuteQuery(queryChoice, parmString);
                    if (bOk) {
                        colHeaders = queryrunner.GetQueryHeaders();
                        queryData = queryrunner.GetQueryData();
                        nullValReturn = (queryData[0][0].equals(""));

                        // Display results if the return row is not null
                        if(!nullValReturn) {
                            for (int i = 0; i < colHeaders.length; i++) {
                                System.out.printf("%-26s", colHeaders[i]);
                                line += "--------------------------";
                            }
                            System.out.println("\n" + line);
                            for (int i = 0; i < queryData.length; i++) {
                                for (int j = 0; j < queryData[i].length; j++) {
                                    System.out.printf("%-26s", queryData[i][j]);
                                }
                                System.out.println();
                            }
                            System.out.println();
                        }
                        // Otherwise, display headers and empty result set
                        else {
                            for (int i = 0; i < colHeaders.length; i++) {
                                System.out.printf("%-26s", colHeaders[i]);
                                line += "--------------------------";
                            }
                            System.out.println("\n" + line);
                            System.out.println("No Results returned!\n");
                        }
                    }
                    else {
                        System.out.println(queryrunner.GetError());
                    }
                }
            }
        } while (choice != 1);
    }

    /**
     * Welcome method that provides on screen information to the suer
     */
    public static void welcome() {
        String welcome = "Welcome the MyRaceSignUp.com console application!\n" +
                "Follow the on screen prompts to access the database" +
                " and run a handful of queries.\nEnter '1' to quit " +
                "from each section. Enjoy!";
        System.out.println(welcome);
    }

    /**
     * Prints the main menu to the screen
     */
    public static void printMainMenu() {
        System.out.println();
        System.out.println("======MYRACESIGNUP.COM======");
        System.out.println("============================");
        System.out.println("1. Quit");
        System.out.println("2. Run Queries");
    }

    /**
     * Prints the menu containing the available queries to run
     *
     * @param qNames string array of the available query names to run
     */
    public static void printQueryMenu(String[] qNames) {
        int numQueries = qNames.length;

        System.out.println("======MYRACESIGNUP.COM======");
        System.out.println("============================");
        System.out.println("1. Quit");

        for (int i = 0; i < numQueries; i++) {
            System.out.println(i + 2 + ". " + qNames[i]);
        }
    }

    /**
     * Prompts the user to enter a number based on values from the on-screen
     * prompt and returns the user input as long as it is a number, otherwise
     * the user it prompted until it is correct.
     *
     * @return number that the user entered.
     */
    public static int getChoice() {
        String choice;  // string value of the user choice
        int ch; // converted integer value of the line
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter choice: ");
        choice = sc.nextLine();
        try {
            ch = Integer.parseInt(choice);
            System.out.println();
        } catch (NumberFormatException e) {
            System.out.println("Please enter a number only!");
            ch = getChoice();
        }
        return ch;
    }
}

