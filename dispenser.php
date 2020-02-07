<?php
define('DISPENSER', '/usr/bin/sudo /home/pi/dispenser');
define('ADDRESS', '192.168.0.22');
define('USER', 'pi');
define('PASSWORD', 'password');
if (!is_null($_GET['flavor']) && !is_null($_GET['time'])) {
	$sconnection = ssh2_connect(ADDRESS, 22);
	ssh2_auth_password($sconnection, USER, PASSWORD);
	$command = DISPENSER.' '.$_GET['flavor'].' '.$_GET['time'];
	$stdio_stream = ssh2_exec($sconnection, $command);
}
?>