package golang

import "github.com/zond/tesla"

type Connection struct {
	client *tesla.Client

	HasErr bool
	Err    string
}

func Connect(email, password string) *Connection {
	client, err := tesla.NewClient(
		&tesla.Auth{
			ClientID:     "e4a9949fcfa04068f59abb5a658f2bac0a3428e4652315490b659d5ab3f35a9e",
			ClientSecret: "c75f14bbadc8bee3a7594412c31416f8300256d7668ea7e6e7f06727bfb9d220",
			Email:        email,
			Password:     password,
		})
	result := &Connection{}
	if err == nil {
		result.client = client
	} else {
		result.HasErr = true
		result.Err = err.Error()
	}
	return result
}

type Vehicle struct {
	ID   int64
	Name string
}

type Vehicles struct {
	Content *Vehicle
	HasErr  bool
	Err     string
	Next    *Vehicles
}

func (c *Connection) Vehicles() *Vehicles {
	vehicles, err := c.client.Vehicles()
	if err != nil {
		return &Vehicles{
			HasErr: true,
			Err:    err.Error(),
		}
	}
	result := &Vehicles{}
	next := result
	for index, vehicle := range vehicles {
		next.Content = &Vehicle{
			ID:   vehicle.Vehicle.ID,
			Name: vehicle.Vehicle.DisplayName,
		}
		if index < len(vehicles)-1 {
			next.Next = &Vehicles{}
			next = next.Next
		}
	}
	return result
}
