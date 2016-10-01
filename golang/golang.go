package golang

import "github.com/zond/tesla"

type Connection struct {
	client *tesla.Client
}

func Connect(email, password string) (*Connection, error) {
	client, err := tesla.NewClient(
		&tesla.Auth{
			ClientID:     "e4a9949fcfa04068f59abb5a658f2bac0a3428e4652315490b659d5ab3f35a9e",
			ClientSecret: "c75f14bbadc8bee3a7594412c31416f8300256d7668ea7e6e7f06727bfb9d220",
			Email:        email,
			Password:     password,
		})
	if err != nil {
		return nil, err
	}
	return &Connection{
		client: client,
	}, nil
}

type Vehicle struct {
	ID   int64
	Name string

	vehicle *tesla.Vehicle
}

func (v *Vehicle) MobileEnabled() (bool, error) {
	return v.vehicle.MobileEnabled()
}

type Vehicles struct {
	Content *Vehicle
	Next    *Vehicles
}

func (c *Connection) Vehicles() (*Vehicles, error) {
	vehicles, err := c.client.Vehicles()
	if err != nil {
		return nil, err
	}
	result := &Vehicles{}
	next := result
	for index, vehicle := range vehicles {
		next.Content = &Vehicle{
			ID:      vehicle.Vehicle.ID,
			Name:    vehicle.Vehicle.DisplayName,
			vehicle: vehicle.Vehicle,
		}
		if index < len(vehicles)-1 {
			next.Next = &Vehicles{}
			next = next.Next
		}
	}
	return result, nil
}
