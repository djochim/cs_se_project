# Hetzner cloud
variable "hcloud_token" {
  type        = string
  sensitive = true
  description = "Hetzner cloud API token"
}

variable "server_name" {
  type        = string
  description = "Hetzner cloud server name"
  default = "aeon-server"
}

variable "server_type" {
  type        = string
  description = "Hetzner cloud server type"
  default = "cx22"
}

variable "server_location" {
  type        = string
  description = "Hetzner cloud server location"
  default = "fsn1"
}

# OS configuration
variable "appname" {
  default = "aeon"
}