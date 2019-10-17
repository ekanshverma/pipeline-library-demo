import groovy.json.JsonBuilder  
import groovy.json.JsonSlurper  

def cleanWorkspace()
    {
        echo "Cleaning up ${WORKSPACE}"
        // clean up our workspace 
        deleteDir()
        // clean up tmp directory 
        dir("${workspace}@tmp") {
            deleteDir()            
        }
    }
def getECRName(){
    String repo = steps.sh(returnStdout: true, script: "aws ecr get-login --region us-west-2 --no-include-email | cut -d \" \" -f 7" ).trim()
    return repo.replace("https://", "")
}



