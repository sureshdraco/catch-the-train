<?php

	//create an array of data with a key value pair for a title and description
	$dataArray = array(array('title' => 'title 1', 'description' => 'description 1 full text'), array('title' => 'title 2', 'description' => 'description 2 full text'));

	//Nest our data array within a success wrapper. We will use this to verify that we have a valid response on the client side
	$finalResponse = array('success' => 'YES', 'data' => $dataArray);

	//We are just going to wait 3 seconds to simulat a longer response and how to handle that correctly on the client
	sleep(3);
	
	echo json_encode($finalResponse);

?>