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
        m_querynames = new String[10];
        m_queryParamNames = new String[10];


        // TODO - You will need to change the queries below to match your queries.

        // You will need to put your Project Application in the below variable

        this.m_projectTeamApplication = "MyRaceSignUp.com";    // THIS NEEDS TO CHANGE FOR YOUR APPLICATION

        // Each row that is added to m_queryArray is a separate query. It does not work on Stored procedure calls.
        // The 'new' Java keyword is a way of initializing the data that will be added to QueryArray. Please do not change
        // Format for each row of m_queryArray is: (QueryText, ParamaterLabelArray[], LikeParameterArray[], IsItActionQuery, IsItParameterQuery)

        //    QueryText is a String that represents your query. It can be anything but Stored Procedure
        //    Parameter Label Array  (e.g. Put in null if there is no Parameters in your query, otherwise put in the Parameter Names)
        //    LikeParameter Array  is an array I regret having to add, but it is necessary to tell QueryRunner which parameter has a LIKE Clause. If you have no parameters, put in null. Otherwise put in false for parameters that don't use 'like' and true for ones that do.
        //    IsItActionQuery (e.g. Mark it true if it is, otherwise false)
        //    IsItParameterQuery (e.g.Mark it true if it is, otherwise false)

//        m_queryArray.add(new QueryData("Select * from contact", null, null, false, false));   // THIS NEEDS TO CHANGE FOR YOUR APPLICATION
//        m_queryArray.add(new QueryData("Select * from contact where contact_id=?", new String [] {"CONTACT_ID"}, new boolean [] {false},  false, true));        // THIS NEEDS TO CHANGE FOR YOUR APPLICATION
//        m_queryArray.add(new QueryData("Select * from contact where contact_name like ?", new String [] {"CONTACT_NAME"}, new boolean [] {true}, false, true));        // THIS NEEDS TO CHANGE FOR YOUR APPLICATION
//        m_queryArray.add(new QueryData("insert into contact (contact_id, contact_name, contact_salary) values (?,?,?)",new String [] {"CONTACT_ID", "CONTACT_NAME", "CONTACT_SALARY"}, new boolean [] {false, false, false}, true, true));// THIS NEEDS TO CHANGE FOR YOUR APPLICATION

        // TEMPLATE:  m_queryArray.add(new QueryData("QUERY", null, null, false, false));

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
        // TODO
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

        m_querynames[8] = "Players born after 2000's result";

        /**
         * Query #10  Events in Washington state that take place between input dates. -- parameter
         */
        // TODO
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

    // Returns the query names array
    public String[] getNameArray() {
        String[] copy = new String[10];
        for (int i = 0; i < 10; i++) {
            copy[i] = m_querynames[i];
        }

        return copy;
    }

    // Returns the query param names
    public String[] getParamArray() {
        String[] copy = new String[10];
        for (int i = 0; i < 10; i++) {
            copy[i] = m_queryParamNames[i];
        }

        return copy;
    }

    private QueryJDBC m_jdbcData;
    private String m_error;
    private String m_projectTeamApplication;
    private ArrayList<QueryData> m_queryArray;
    private int m_updateAmount;
    private String[] m_querynames;
    private String[] m_queryParamNames;
    public static final int QUIT = 1;

    /**
     * @param args the command line arguments
     */


    public static void main(String[] args) {
        // TODO code application logic here

        final QueryRunner queryrunner = new QueryRunner();

        if (args.length == 0) {
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {

                    new QueryFrame(queryrunner).setVisible(true);
                }
            });
        } else {
            if (args[0].equals("-console")) {

                // TODO
                // You should code the following functionality:

                //    You need to determine if it is a parameter query. If it is, then
                //    you will need to ask the user to put in the values for the Parameters in your query
                //    you will then call ExecuteQuery or ExecuteUpdate (depending on whether it is an action query or regular query)
                //    if it is a regular query, you should then get the data by calling GetQueryData. You should then display this
                //    output. 
                //    If it is an action query, you will tell how many row's were affected by it.
                // 
                //    This is Psuedo Code for the task:  
                //    Connect()
                //    n = GetTotalQueries()
                //    for (i=0;i < n; i++)
                //    {
                //       Is it a query that Has Parameters
                //       Then
                //           amt = find out how many parameters it has
                //           Create a paramter array of strings for that amount
                //           for (j=0; j< amt; j++)
                //              Get The Paramater Label for Query and print it to console. Ask the user to enter a value
                //              Take the value you got and put it into your parameter array
                //           If it is an Action Query then
                //              call ExecuteUpdate to run the Query
                //              call GetUpdateAmount to find out how many rows were affected, and print that value
                //           else
                //               call ExecuteQuery 
                //               call GetQueryData to get the results back
                //               print out all the results
                //           end if
                //      }
                //    Disconnect()


                // NOTE - IF THERE ARE ANY ERRORS, please print the Error output
                // NOTE - The QueryRunner functions call the various JDBC Functions that are in QueryJDBC. If you would rather code JDBC
                // functions directly, you can choose to do that. It will be harder, but that is your option.
                // NOTE - You can look at the QueryRunner API calls that are in QueryFrame.java for assistance. You should not have to 
                //    alter any code in QueryJDBC, QueryData, or QueryFrame to make this work.

                System.out.println("Welcome to the DB, connecting....");
                Scanner scanner = new Scanner(System.in);


                String cont = " ";
                String szHost = "cs100";
                String szUser = "mm_cpsc502101team02";
                String szPass = "mm_cpsc502101team02Pass-";
                String szDatabase = "mm_cpsc502101team02";

//                System.out.println("Enter the Hostname: ");
//                szHost = scanner.nextLine();
//                System.out.println("Enter the Username: ");
//                szUser = scanner.nextLine();
//                System.out.println("Enter the Password: ");
//                szPass = scanner.nextLine();
//                System.out.println("Enter the Database: ");
//                szDatabase = scanner.nextLine();

                Scanner sc = new Scanner(System.in);
                int choice;
                boolean connected;  // Successful connection to the DB

                // Connect to the database
                connected = queryrunner.Connect(szHost, szUser, szPass, szDatabase);
                if (connected) {
                    System.out.println("Connected!");
                }

                do {

                    // Print the menu choice screen, until the user selects quit
                    printMainMenu();
                    // Get the choice from the user
                    choice = getChoice();

                    // add
                    if (choice == 2) {
                        // Enter query screen
                        enterQueryScreen(queryrunner);
                    }
                    while (choice > 2 || choice < 1) {
                        System.out.println("Invalid number! Please enter again: ");
                        choice = getChoice();
                    }

                } while (choice != 1);

                // Disconnect from the database
                queryrunner.Disconnect();
            }
        }
    }


    public static void enterQueryScreen(QueryRunner queryrunner) {
        String[] colHeaders;  // column names
        String[][] queryData;  // query result set
        String[] qNames = queryrunner.getNameArray();
        String[] pNames = queryrunner.getParamArray();
        String paramInput;
        int paramAmnt;  // Number of parameters in the query
        int choice;  // Choice of the query to run from the user
        int queryChoice;  // index of query chosen
        int rowsAffected;
        boolean bOk = true;
        Scanner sc = new Scanner(System.in);

        do {
            String[] parmString = {};  // array of parameters

            // Print the query menu choices
            printQueryMenu(qNames);

            // Get the choice from the user

            choice = getChoice();
            // add one more condition
            while (choice > 11 || choice < 1) {
                System.out.println("Invalid number! Please enter again: ");
                choice = getChoice();
            }
            // Go into query parsing if not equal to quit value
            if (choice < 12 && choice > 0) {
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
                }

                // If an action query, execute the action
                if (queryrunner.isActionQuery(queryChoice)) {
                    bOk = queryrunner.ExecuteUpdate(queryChoice, parmString);
                    if (bOk) {
                        rowsAffected = queryrunner.GetUpdateAmount();
                    } else {
                        System.out.println(queryrunner.GetError());
                    }
                }
                // Otherwise, is a parameter query
                else {
                    String line = "";
                    bOk = queryrunner.ExecuteQuery(queryChoice, parmString);
                    if (bOk) {
                        colHeaders = queryrunner.GetQueryHeaders();
                        queryData = queryrunner.GetQueryData();
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
                }
            }
        } while (choice != 1);
    }

    public static void printMainMenu() {
        System.out.println();
        System.out.println("======MYRACESIGNUP.COM======");
        System.out.println("============================");
        System.out.println("1. Quit");
        System.out.println("2. Run Queries");
    }

    public static void printQueryMenu(String[] qNames) {
        System.out.println("======MYRACESIGNUP.COM======");
        System.out.println("============================");
        System.out.println("1. Quit");

        for (int i = 0; i < 10; i++) {
            System.out.println(i + 2 + ". " + qNames[i]);
        }
    }

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


