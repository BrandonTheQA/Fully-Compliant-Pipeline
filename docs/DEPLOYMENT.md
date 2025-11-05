# Deployment and GitHub Actions Setup

This repository deploys the `order`, `product`, and `user` Spring Boot services to both Azure Kubernetes Service (AKS) and AWS Elastic Kubernetes Service (EKS) using GitHub Actions. Both workflows run in parallel and independently.

## AKS Deployment

### Azure resources
- AKS: existing cluster in resource group `RG-POC` (cluster name discovered at deploy time)
- ACR: `rgpocacr9021` (`rgpocacr9021.azurecr.io`)

### GitHub configuration
Define the following Repository Variables (Actions → Variables):
- `AZURE_SUBSCRIPTION_ID`: Subscription GUID
- `AZURE_TENANT_ID`: Entra tenant GUID
- `AZURE_CLIENT_ID`: App registration (workload identity) client ID
- `AKS_RESOURCE_GROUP`: `RG-POC` (optional)

No classic Secrets are required if using OIDC with `azure/login`.

### Entra ID and role assignments
Create or use an existing App Registration for GitHub Actions and configure a Federated Credential:
- Entity: GitHub Actions
- Repository: this repo
- Branch: `main`
- Subject: `repo:<owner>/<repo>:ref:refs/heads/main`

Grant the identity permissions:
- On ACR `rgpocacr9021`: `AcrPush`
- On AKS cluster (resource scope): `Azure Kubernetes Service RBAC Writer` (or `Contributor` during bootstrap)

### Workflow
- File: `.github/workflows/ci-cd.yml`
- On push/PR to `main`:
  - Build and push Docker images to ACR with tags `${{ github.sha }}` and `latest`
  - Discover AKS in `RG-POC`, set kube context
  - Ensure namespace `dev` exists; apply manifests in `k8s/*`
  - Update Deployments to the `${{ github.sha }}` image tags

### Kubernetes
- Namespace: `dev`
- Services: `LoadBalancer` type for external access
- Manifests in `k8s/<service>/{deployment,service}.yaml`
- Health checks: `/actuator/health`

### Environments beyond dev (TODOs)
- Add `staging` and `prod` jobs to the workflow:
  - Namespaces: `staging`, `prod`
  - Manual approvals/environments
  - Optional Ingress and TLS per environment
  - Image promotion via retagging (avoid rebuilds)

### Accessing services
After deployment, retrieve external IPs:
```
kubectl get svc -n dev
```
Endpoints will be on port 80 forwarding to container port 8080.

### Build Optimization
- Maven dependencies are cached in GitHub Actions to speed up build times
- Cache is automatically invalidated when pom.xml files change

---

## EKS Deployment

This repository also deploys the same services to AWS Elastic Kubernetes Service (EKS) using a separate GitHub Actions workflow that runs in parallel with the AKS deployment.

### AWS resources
- EKS: existing cluster (cluster name and region configured via secrets)
- ECR: Elastic Container Registry for Docker images

### GitHub configuration
Define the following Repository Secrets (Actions → Secrets and variables → Actions → Secrets):
- `AWS_ACCESS_KEY_ID`: AWS access key ID for authentication
- `AWS_SECRET_ACCESS_KEY`: AWS secret access key
- `AWS_REGION`: AWS region where EKS cluster is located (e.g., `us-east-1`)
- `ECR_REGISTRY`: ECR registry URL (e.g., `123456789012.dkr.ecr.us-east-1.amazonaws.com`)
- `EKS_CLUSTER_NAME`: Name of the EKS cluster
- `SQL_SERVER_PASSWORD`: Database password (shared with AKS workflow)

#### How to obtain AWS credentials

**1. AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY:**

These are created through AWS IAM (Identity and Access Management):

1. **Log into AWS Console** and navigate to **IAM** service
2. Go to **Users** → Click **Create user** (or select an existing user)
3. Enter a username (e.g., `github-actions-eks`)
4. **Skip console access** (not needed for programmatic access) → **Next**
5. **Attach policies directly**:
   - **Required policies:**
     - `AmazonEC2ContainerRegistryFullAccess` (for ECR push/pull operations)
     - `AmazonEKSClusterPolicy` (for EKS cluster access - **REQUIRED** for `eks:DescribeCluster` and `eks:ListClusters`)
   - Or create a custom policy with the permissions listed in "AWS IAM permissions" section below
   - **Important:** The `AmazonEKSClusterPolicy` is essential for the `aws eks update-kubeconfig` command to work
6. Click **Create user**
7. **Create Access Keys**:
   - Select the user you just created
   - Go to **Security credentials** tab
   - Scroll down to **Access keys** section
   - Click **Create access key**
   - Select **Application running outside AWS** as the use case
   - Click **Next**, add a description (optional), then **Create access key**
   - **IMPORTANT**: Copy both values immediately:
     - **Access key ID** → This is your `AWS_ACCESS_KEY_ID`
     - **Secret access key** → This is your `AWS_SECRET_ACCESS_KEY`
   - ⚠️ **Warning**: The secret access key is shown only once. Save it securely!

**2. AWS_REGION:**

The AWS region is where your EKS cluster is deployed:

1. **Find your EKS cluster region**:
   - Go to **Amazon EKS** service in AWS Console
   - Your cluster name will show the region in the URL or cluster details
   - Or check the region dropdown in the top-right of AWS Console
2. **Common regions**:
   - `us-east-1` (N. Virginia)
   - `us-west-2` (Oregon)
   - `eu-west-1` (Ireland)
   - `ap-southeast-1` (Singapore)
   - See [AWS Regions list](https://docs.aws.amazon.com/general/latest/gr/rande.html#eks_region) for all options

**3. Additional secrets:**

- **ECR_REGISTRY**: 
  - Format: `<AWS_ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com`
  - Find your AWS Account ID in the top-right of AWS Console (click your username)
  - Example: `123456789012.dkr.ecr.us-east-1.amazonaws.com`

- **EKS_CLUSTER_NAME**: 
  - Go to **Amazon EKS** service in AWS Console
  - The cluster name is displayed in the clusters list

**Adding secrets to GitHub:**

1. Go to your GitHub repository
2. Navigate to **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**
4. Add each secret:
   - Name: `AWS_ACCESS_KEY_ID`, Value: (paste your access key ID)
   - Name: `AWS_SECRET_ACCESS_KEY`, Value: (paste your secret access key)
   - Name: `AWS_REGION`, Value: (e.g., `us-east-1`)
   - Name: `ECR_REGISTRY`, Value: (your ECR registry URL)
   - Name: `EKS_CLUSTER_NAME`, Value: (your EKS cluster name)

### AWS IAM permissions
The AWS credentials used by GitHub Actions require the following permissions:
- ECR: `ecr:GetAuthorizationToken`, `ecr:BatchCheckLayerAvailability`, `ecr:GetDownloadUrlForLayer`, `ecr:BatchGetImage`, `ecr:PutImage`, `ecr:InitiateLayerUpload`, `ecr:UploadLayerPart`, `ecr:CompleteLayerUpload`, `ecr:DescribeRepositories`, `ecr:CreateRepository`
- EKS: `eks:DescribeCluster`, `eks:ListClusters`
- EKS cluster access: The IAM user/role must have permissions to update kubeconfig and access the EKS cluster

#### Creating a custom IAM policy for EKS deployment

If you encounter an `AccessDeniedException` when calling `DescribeCluster`, the IAM user needs additional permissions. Create a custom policy with the following JSON:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "eks:DescribeCluster",
        "eks:ListClusters"
      ],
      "Resource": "*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "ecr:GetAuthorizationToken",
        "ecr:BatchCheckLayerAvailability",
        "ecr:GetDownloadUrlForLayer",
        "ecr:BatchGetImage",
        "ecr:PutImage",
        "ecr:InitiateLayerUpload",
        "ecr:UploadLayerPart",
        "ecr:CompleteLayerUpload",
        "ecr:DescribeRepositories",
        "ecr:CreateRepository"
      ],
      "Resource": "*"
    }
  ]
}
```

**To attach this policy to your IAM user:**

1. **Go to AWS Console** → **IAM** → **Users**
2. **Select your user** (e.g., `k8s`)
3. Click **Add permissions** → **Create inline policy** (or attach an existing policy)
4. Switch to **JSON** tab and paste the policy above
5. **Review and save** the policy

**Alternatively, attach AWS managed policies:**

- `AmazonEKSClusterPolicy` - Provides `eks:DescribeCluster` and `eks:ListClusters`
- `AmazonEC2ContainerRegistryFullAccess` - Provides all ECR permissions

**Note:** The `aws eks update-kubeconfig` command requires `eks:DescribeCluster` to retrieve cluster endpoint and certificate data. Without this permission, the deployment will fail at the "Configure kubectl for EKS" step.

#### EKS Cluster Authentication (Kubernetes RBAC)

After granting AWS IAM permissions, you also need to grant Kubernetes API access to your IAM user. EKS uses the `aws-auth` ConfigMap to map IAM users/roles to Kubernetes users.

**If you get an error like:**
```
error validating data: failed to download openapi: the server has asked for the client to provide credentials
```

This means your IAM user has AWS permissions but is not mapped to a Kubernetes user in the EKS cluster.

**To fix this, add your IAM user to the `aws-auth` ConfigMap:**

1. **Get the current aws-auth ConfigMap:**
   ```bash
   kubectl get configmap aws-auth -n kube-system -o yaml > aws-auth.yaml
   ```
   (Note: You'll need cluster admin access to do this initially)

2. **Edit `aws-auth.yaml` and add your IAM user under `mapUsers`:**
   ```yaml
   apiVersion: v1
   kind: ConfigMap
   metadata:
     name: aws-auth
     namespace: kube-system
   data:
     mapUsers: |
       - userarn: arn:aws:iam::168034219143:user/k8s
         username: k8s-user
         groups:
           - system:masters
     mapRoles: |
       # ... existing roles ...
   ```
   
   **Note:** `system:masters` grants cluster-admin access. For production, use a more restrictive role like `system:authenticated` and bind it to specific RBAC roles.

3. **Apply the updated ConfigMap:**
   ```bash
   kubectl apply -f aws-auth.yaml
   ```

**Alternative: Use IAM Role instead of IAM User (Recommended)**

For better security and easier management, consider using an IAM role instead:

1. Create an IAM role (e.g., `github-actions-eks-role`)
2. Attach the same policies (`AmazonEKSClusterPolicy`, `AmazonEC2ContainerRegistryFullAccess`)
3. Add the role to `aws-auth` ConfigMap under `mapRoles`:
   ```yaml
   mapRoles: |
     - rolearn: arn:aws:iam::168034219143:role/github-actions-eks-role
       username: github-actions
       groups:
         - system:masters
   ```
4. Update GitHub Actions secrets to use role-based authentication (requires additional setup for role assumption)

**For production environments, create a restricted RBAC role:**

Instead of `system:masters`, create a custom role with only the permissions needed:

```bash
# Create a ClusterRole with necessary permissions
kubectl apply -f - <<EOF
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: github-actions-deployer
rules:
# Namespace management
- apiGroups: [""]
  resources: ["namespaces"]
  verbs: ["get", "list", "watch", "create", "update", "patch"]
# ConfigMaps (for deployment state)
- apiGroups: [""]
  resources: ["configmaps"]
  verbs: ["get", "list", "watch", "create", "update", "patch", "delete"]
# Secrets (for database credentials)
- apiGroups: [""]
  resources: ["secrets"]
  verbs: ["get", "list", "watch", "create", "update", "patch"]
# Deployments and ReplicaSets
- apiGroups: ["apps"]
  resources: ["deployments", "replicasets"]
  verbs: ["get", "list", "watch", "create", "update", "patch", "delete"]
# Services
- apiGroups: [""]
  resources: ["services"]
  verbs: ["get", "list", "watch", "create", "update", "patch", "delete"]
# Pods (for health checks and rollout status)
- apiGroups: [""]
  resources: ["pods"]
  verbs: ["get", "list", "watch"]
EOF

# Create ClusterRoleBinding
kubectl apply -f - <<EOF
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: github-actions-deployer-binding
subjects:
- kind: User
  name: k8s-user
  apiGroup: rbac.authorization.k8s.io
roleRef:
  kind: ClusterRole
  name: github-actions-deployer
  apiGroup: rbac.authorization.k8s.io
EOF
```

Then use `system:authenticated` in the aws-auth ConfigMap instead of `system:masters`.

**Note:** If you're still getting permission errors after adding the user to `aws-auth`, verify:
1. The `aws-auth` ConfigMap was applied successfully: `kubectl get configmap aws-auth -n kube-system -o yaml`
2. The IAM user ARN matches exactly: `arn:aws:iam::168034219143:user/k8s`
3. The groups include `system:masters` (for full access) or the custom RBAC role is properly bound
4. Wait a few seconds after updating `aws-auth` for changes to propagate

### Workflow
- File: `.github/workflows/ci-cd-eks.yml`
- On push/PR to `main`:
  - Build and push Docker images to ECR with tags `${{ github.sha }}` and `latest`
  - Create ECR repositories if they don't exist (order, product, user, ui)
  - Configure kubectl for EKS cluster using `aws eks update-kubeconfig`
  - Ensure namespace `dev` exists; apply manifests in `k8s/*`
  - Update Deployments to the `${{ github.sha }}` image tags from ECR

### Kubernetes
- Namespace: `dev` (same structure as AKS)
- Services: `LoadBalancer` type for external access
- Manifests: Reuses the same manifests in `k8s/<service>/{deployment,service}.yaml`
- Health checks: `/actuator/health` for API services, `/health` for UI
- Blue-green deployment: Same deployment pattern as AKS with deployment-color labels

### Parallel execution
- Both AKS and EKS workflows trigger on the same events
- Workflows run independently and can execute simultaneously
- No dependencies between the two workflows
- Each workflow manages its own container registry and cluster

### Accessing services
After deployment, retrieve external IPs:
```
aws eks update-kubeconfig --region <region> --name <cluster-name>
kubectl get svc -n dev
```
Endpoints will be on port 80 forwarding to container port 8080.
