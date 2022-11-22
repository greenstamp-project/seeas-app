When the app runs the first time will ask for permissions to create and read files on external storage.
After that run the app again, it will creates a file called 'parameters.txt' on Sdcard\Downloads folder

Edit that file to match the test parameters and the run the app again.

Content of parameters.txt file:
> function=makeFile<br />
fileToRead=somefile.txt<br />
timesToRun=1

| Parameter | Possible Values | Description |
| :------------ |:---------------| :-----|
| function |makeFile, saveCloud| *makeFile*: create copies of the selected file on the Downloads folder writen on the fileToRead param *saveCloud*: sends the copies of the selected file on the Downloads folder to the firebase storage|
|fileToRead|filename|the name of the file to be read, the file should be placed in the Downloads folder|
|timesToRun|a number|The number of times the function should perform|
