# EKS Overlay Configuration

This overlay configures deployments for Amazon EKS (Elastic Kubernetes Service) to use AWS ECR (Elastic Container Registry).

## Configuration

The overlay automatically transforms image references from Azure Container Registry (ACR) to AWS ECR using Kustomize image transformations.

## Setup Instructions

### 1. Configure ECR Repository Details

Before deploying, update `kustomization.yaml` with your AWS account ID and region:

```yaml
images:
  - name: rgpocacr9021.azurecr.io/order
    newName: <AWS_ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com/order
  # ... etc
```

Replace:
- `<AWS_ACCOUNT_ID>` with your AWS account ID (12-digit number)
- `<REGION>` with your AWS region (e.g., `us-east-1`, `us-west-2`)

### 2. Ensure ECR Repositories Exist

Make sure your ECR repositories exist in your AWS account:

```bash
# Create repositories if they don't exist
aws ecr create-repository --repository-name order --region <REGION>
aws ecr create-repository --repository-name product --region <REGION>
aws ecr create-repository --repository-name user --region <REGION>
aws ecr create-repository --repository-name ui --region <REGION>
```

### 3. Push Images to ECR

Push your images to ECR (after building/tagging):

```bash
# Login to ECR
aws ecr get-login-password --region <REGION> | docker login --username AWS --password-stdin <AWS_ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com

# Tag and push images
docker tag rgpocacr9021.azurecr.io/order:latest <AWS_ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com/order:latest
docker push <AWS_ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com/order:latest

# Repeat for product, user, and ui
```

### 4. IAM Permissions

EKS automatically uses IAM roles for ECR authentication. Ensure your EKS node group or pod service account has the necessary IAM permissions:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "ecr:GetAuthorizationToken",
        "ecr:BatchCheckLayerAvailability",
        "ecr:GetDownloadUrlForLayer",
        "ecr:BatchGetImage"
      ],
      "Resource": "*"
    }
  ]
}
```

If using IRSA (IAM Roles for Service Accounts), attach this policy to your service account's IAM role.

## Deployment

After configuring the image registry details:

```bash
kubectl apply -k k8s/overlays/eks/
```

## Features

- **Automatic Image Transformation**: ACR image references are automatically transformed to ECR
- **IAM Authentication**: Uses IAM roles for ECR authentication (no secrets required)
- **No Node Pool Constraints**: Deployments can schedule on any available nodes
- **Cloud-Agnostic Tolerations**: Includes tolerations for EKS CriticalAddonsOnly taint
