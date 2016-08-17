<?php
	class DB_CONNECT {
 
    // constructor
    function __construct() {
        // connecting to database
        $this->connect();
    }
 
    // destructor
    function __destruct() {
        // closing db connection
        $this->close();
    }
 
    /**
     * Function to connect with database
     */
    function connect() {
        //connection variables
        //require_once __DIR__ . '/config.php';
		define("DB_HOST", "mysql.hostinger.com.br");
		define("DB_USER", "u289548429_tgadm");
		define("DB_PASSWORD", "Daisastofmi02.");
		define("DB_DATABASE", "u289548429_ads");
 
        // Connecting to mysql database
        $con = @mysql_connect(DB_HOST, DB_USER, DB_PASSWORD) or die(mysql_error());
		//$con = mysqli_connect(DB_HOST, DB_USER, DB_PASSWORD, DB_DATABASE) or die(mysql_error());
 
        // Selecing database
        $db = @mysql_select_db(DB_DATABASE) or die(mysql_error()) or die(mysql_error());
 
        // returing connection cursor
        return $con;
    }
 
    /**
     * Function to close db connection
     */
    function close() {
        // closing db connection
		@mysql_close($con);
		//mysqli_close($con);
		
    }
	
	
 
}
?>