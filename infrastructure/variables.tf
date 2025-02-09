# Hetzner cloud
variable "hcloud_token" {
  type        = string
  sensitive   = true
  description = "Hetzner cloud API token"
}

variable "server_name" {
  type        = string
  description = "Hetzner cloud server name"
  default     = "aeon-server"
}

variable "server_type" {
  type        = string
  description = "Hetzner cloud server type"
  default     = "cx22"
}

variable "server_location" {
  type        = string
  description = "Hetzner cloud server location"
  default     = "fsn1"
}

# OS configuration
variable "appname" {
  type        = string
  description = "Flatcar appname"
  default     = "aeon"
}

variable "developer_ssh" {
  type        = string
  sensitive   = true
  description = "SSH public key for developer access"
}

# cloudflare
variable "cloudflare_zone_id" {
  type        = string
  sensitive   = true
  description = "Cloudflare API zone_id"
}
variable "cloudflare_token" {
  type        = string
  sensitive   = true
  description = "Cloudflare API token"
}
variable "domain" {
  type        = string
  description = "Server domain"
  default     = "jochim.dev"
}
variable "ip" {
  type        = string
  description = "Server domain"
  default     = "49.13.218.105"
}