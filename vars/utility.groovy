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
def createDockerRegistry(def dockerRepo) {
    def sout = new StringBuilder(), serr = new StringBuilder()
    command = "aws ecr describe-repositories --region us-west-2"
    def proc = command.execute()
    proc.consumeProcessOutput(sout, serr)
    proc.waitForOrKill(10000)
    if ("$serr"){
        return "Unable to find registry information error is ${serr}"
    }
    else{
        if("$sout"){
            def str = "${sout}"
            def parser = new JsonSlurper()
            def json = parser.parseText(str)
            def reg_found = false
            assert json instanceof Map
            json.repositories.repositoryName.each{ 
                v -> if(v.toString().contains(dockerRepo)){
                    reg_found = true
                }
            }
            if(reg_found == true){
                println("Registry exists")
            }
            else{
                println("Creating registry now ...")
                def sout_rc = new StringBuilder(), serr_rc = new StringBuilder()
                command = "aws ecr create-repository --repository-name ${dockerRepo} --region us-west-2"
                def proc_rc = command.execute()
                proc_rc.consumeProcessOutput(sout_rc, serr_rc)
                proc.waitForOrKill(10000)
                if(serr){
                    println("Error creating repository")
                    return
                }
            }
        }
        else{
            println("Creating registry now ...")
            def sout_rc = new StringBuilder(), serr_rc = new StringBuilder()
            command = "aws ecr create-repository --repository-name ${dockerRepo} --region us-west-2"
            def proc_rc = command.execute()
            proc_rc.consumeProcessOutput(sout_rc, serr_rc)
            proc.waitForOrKill(10000)
            if(serr){
                println("Error creating repository")
                return "${sout_rc} : ${$err_rc}"
            }
            return "${sout_rc} : ${serr_rc}"
        }
    }
}

