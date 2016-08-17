<?php
	// array for JSON response
	$response = array();
 
	// check for required fields
	if (isset($_GET['latitude']) && isset($_GET['longitude']) && isset($_GET['description'])) {
 
    $latitude = $_GET['latitude'];
    $longitude = $_GET['longitude'];
    $description = $_GET['description'];
 
    // include db connect class
    require_once __DIR__ . '/db_connect.php';
 
    // connecting to db
    $db = new DB_CONNECT();
 
    // mysql inserting a new row
    $result = mysql_query("INSERT INTO geolo(latitude, longitude, description, active) VALUES('$latitude', '$longitude', '$description', 1)");
	//$result = mysql_query("INSERT INTO geolo(latitude, longitude, description, active) VALUES('$latitude', '$longitude', '$description', 0)");
 
    // check if row inserted or not
    if ($result) {
        // successfully inserted into database
        $response["success"] = 1;
        $response["message"] = "Location successfully created.";
 
        // echoing JSON response
        echo json_encode($response);
    } else {
        // failed to insert row
        $response["success"] = 0;
        $response["message"] = "An error occurred.";
 
        // echoing JSON response
        echo json_encode($response);
    }
} else {
    // required field is missing
    $response["success"] = 0;
    $response["message"] = "Required field(s) is missing";
 
    // echoing JSON response
    echo json_encode($response);
}
?>