<?php

// array for JSON response
$response = array();
 
// include db connect class
require_once __DIR__ . '/db_connect.php';
// connecting to db
$db = new DB_CONNECT();
 
// check for post data
if (isset($_GET["latitude"]) && isset($_GET["longitude"])) {
    $latitude = $_GET['latitude'];
	$longitude = $_GET['longitude'];
 
    // get a product from products table
    //$result = mysql_query("select * from geolo where latitude like '$latitude%' and longitude like '$longitude%' and active = 1");
	
	//Get nearby locations
	//$result = mysql_query("SELECT description, latitude, longitude, 111.045* DEGREES(ACOS(COS(RADIANS(latpoint)) * COS(RADIANS(latitude)) * COS(RADIANS(longpoint) - RADIANS(longitude)) + SIN(RADIANS(latpoint)) * SIN(RADIANS(latitude)))) AS distance_in_km FROM geolo JOIN (SELECT  '$latitude'  AS latpoint,  '$longitude' AS longpoint) AS p ON 1=1 ORDER BY distance_in_km LIMIT 20");
	
	//Improved function
    $result = mysql_query("SELECT g.description, g.latitude, g.longitude, p.distance_unit * DEGREES(ACOS(COS(RADIANS(p.latpoint)) * COS(RADIANS(g.latitude)) * COS(RADIANS(p.longpoint) - RADIANS(g.longitude))+ SIN(RADIANS(p.latpoint)) * SIN(RADIANS(g.latitude)))) AS distance_in_km FROM geolo AS g  JOIN ( SELECT  '$latitude'   AS latpoint,  '$longitude' AS longpoint, 20.0 AS radius, 111.045 AS distance_unit) AS p ON 1=1 WHERE g.latitude BETWEEN p.latpoint  - (p.radius / p.distance_unit) AND p.latpoint  + (p.radius / p.distance_unit) AND g.longitude BETWEEN p.longpoint - (p.radius / (p.distance_unit * COS(RADIANS(p.latpoint)))) AND p.longpoint + (p.radius / (p.distance_unit * COS(RADIANS(p.latpoint)))) ORDER BY distance_in_km LIMIT 3");
	
	if (!empty($result)) {
        // check for empty result
       
if (mysql_num_rows($result) > 0) {
    // looping through all results
    // locations node
    $response["locations"] = array();
    
    while ($row = mysql_fetch_array($result)) {
        // temp user array
        $location = array();
        $location["description"] = $row["description"];
        $location["latitude"] = $row["latitude"];
        $location["longitude"] = $row["longitude"];
        $location["distance_in_km"] = $row["distance_in_km"];



        // push single product into final response array
        array_push($response["locations"], $location);
    }
    // success
    $response["success"] = 1;

    // echoing JSON response
    echo json_encode($response);
} 		else {
            // no location found
            $response["success"] = 0;
            $response["message"] = "No location found";
 
            // echo no users JSON
            echo json_encode($response);
        }
    } else {
        // no location found
        $response["success"] = 0;
        $response["message"] = "No location found";
 
        // echo no users JSON
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