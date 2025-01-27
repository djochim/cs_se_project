terraform {
  required_providers {
    hcloud = {
      source  = "hetznercloud/hcloud"
      version = "1.49.1"
    }
    ct = {
      source  = "poseidon/ct"
      version = "0.13.0"
    }
  }
}

provider "ct" {
  # Configuration options
}

provider "hcloud" {
  # Configuration options
  token = var.hcloud_token
}

variable "hcloud_token" {
  sensitive = true
}

variable "appname" {
  default = "aeon"
}

data "ct_config" "flatcar-ignition" {
  content = data.template_file.flatcar-cl-config.rendered
}

data "template_file" "flatcar-cl-config" {
  template = file("${path.module}/flatcar-config.yaml.tmpl")
  vars     = { appname = var.appname }
}

# Create a new server running debian
resource "hcloud_server" "aeon-server" {
  name        = "aeon-server"
  image       = "212121145"
  server_type = "cx22"
  location    = "fsn1"
  user_data   = data.ct_config.flatcar-ignition.rendered
  public_net {
    ipv4_enabled = false
    ipv6_enabled = true
  }
}