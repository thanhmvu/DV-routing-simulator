# DV-routing-simulator
**PROJECT TITLE:** DV-Routing Simulator    
**PURPOSE OF PROJECT:** Simulate a network of routers implementing the distance vector routing algorithm    
**VERSION or DATE:** May 2017      
**AUTHORS:** Thanh Vu & Ha Vu   

**USER INSTRUCTIONS:**  
The source files are located in *./src/* folder. In order to run the app, you should run the .jar file at the root folder.
Input data files for all routers of different networks are located in *input_files/<network-name>*

### To run the simulation:
*java -jar DVRoutingSimulator.jar <input-file> (-reverse)*
ex: *java -jar DVRoutingSimulator.jar input_files/test2/r1.txt*

### Commands
As the simulation is running, allowed commands are:
1. **PRINT** -- print out the current router's distance vector and the distance vectors received from its neighbors
2. **MSG <dst-ip> <dst-port> <msg>** -- send the message msg to a destination router with the specified address(dst-ip and dst-port) 
3. **CHANGE <dst-ip> <dst-port> <new-weight>** -- change the weight between the current router and the destination router to new-weight and inform the destination router about the change
4. **STOP** -- stop this ConsoleReader thread

More details about the project can be found in *Project3Report.pdf*
