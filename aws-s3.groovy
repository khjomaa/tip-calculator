def label = "aws-jenkins-${UUID.randomUUID().toString()}"
def gitRepoUrl = "https://github.com/khjomaa/tip-calculator.git"
def bucket_name = 's3://khalilj-tip-calc-bucket'
def bucket_acl = 'public-read'

podTemplate(label: label,
        containers: [
                containerTemplate(name: 'jnlp', image: 'jenkins/jnlp-slave:alpine'),
                containerTemplate(name: 'aws', image: 'amazon/aws-cli', command: 'cat', ttyEnabled: true)
        ],
        volumes: [
                hostPathVolume(hostPath: '/var/run/docker.sock', mountPath: '/var/run/docker.sock'),
        ],
) {
    node(label) {
        stage('Checkout Repo') {
            git gitRepoUrl
        }

        stage('Deploy ChartMuseum and Add Repo') {
            container('aws') {
                withAWS(credentials: 'aws-credentials', region: 'us-east-1') {
                    sh "aws s3 mb ${bucket_name}"
                    sh "aws s3 cp tip-calculator/app s3://khalilj-tip-calc-bucket --exclude '.git/*' --exclude '*.md' --recursive --acl ${bucket_acl}"
                }
            }
        }
    }
}