import React, { Component } from "react";
import { Card, Button, Table, message, Divider } from "antd";
import { getUsers, deleteUser, editUser, addUser } from "E:/reactmrp/frontend/src/api/user";
import TypingCard from 'E:/reactmrp/frontend/src/components/TypingCard'
import EditUserForm from "./forms/edit-user-form"
import AddUserForm from "./forms/add-user-form"
import * as my from "E:/reactmrp/frontend/src/myserverless/myserverless.js";

const { Column } = Table;
class User extends Component {
  state = {
    users: [],
    editUserModalVisible: false,
    editUserModalLoading: false,
    currentRowData: {},
    addUserModalVisible: false,
    addUserModalLoading: false,
  };
  
  getUsers = async () => { 
    let json=await my.$qryMapList(`#admin select u.userId as id, u.name, ur.roleName as role, r.roleDescription as description  from users u left join userrole ur on ur.userId=u.userId left join roles r on r.roleName=ur.roleName where not (ur.roleName='admin' and ur.userId='developer') order by r.roleLevel`);  
    const { data: users, code: status } = json; 
    if (status === 200) {
      this.setState({
          users
      })
    }
  }
  
//  这是原版使用mock的getUsers方法  
//  getUsers = async () => { 
//      const result = await getUsers()
//      console.log("users",result);
//      const { users, status } = result.data
//      if (status === 0) {
//        this.setState({
//          users
//        })
//      }
//    }
  
  
  handleEditUser = (row) => {
    this.setState({
      currentRowData:Object.assign({}, row),
      editUserModalVisible: true,
    });
  };

  handleDeleteUser = (row) => {
      const { id } = row;
      let result=my.syncData$javaTx(`#admin 
                  if("admin".equals($1) || "developer".equals($1))
                      return "不能删除开发者或管理员用户!";
                  DB.exe("delete from userrole where userId=", DB.que($1));                      
                  DB.exe("delete from users where userId=", DB.que($1));
                  return true;
              `, id);
      if(result===true){
        message.success("删除成功")
        this.getUsers();
      } else {
          message.error(result);
      }
    }
  
//  这是原版使用Mock的handleDeleteUser方法
//  handleDeleteUser = (row) => {
//    const { id } = row
//    if (id === "admin") {
//      message.error("不能删除管理员用户！")
//      return
//    }
//    deleteUser({id}).then(res => {
//      message.success("删除成功")
//      this.getUsers();
//    })
//  }
  
  handleEditUserOk = _ => {
    const { form } = this.editUserFormRef.props;
    form.validateFields((err, values) => {
      if (err) {
        return;
      }
      this.setState({ editModalLoading: true, });
      editUser(values).then((response) => {
        form.resetFields();
        this.setState({ editUserModalVisible: false, editUserModalLoading: false });
        message.success("编辑成功!")
        this.getUsers()
      }).catch(e => {
        message.success("编辑失败,请重试!")
      })
      
    });
  };

  handleCancel = _ => {
    this.setState({
      editUserModalVisible: false,
      addUserModalVisible: false,
    });
  };

  handleAddUser = (row) => {
    this.setState({
      addUserModalVisible: true,
    });
  };

  handleAddUserOk = _ => {
      const { form } = this.addUserFormRef.props;
      form.validateFields(( err, values ) => {
          if ( err ) {
              return;
          }
          console.log("values",values);
          this.setState( { addUserModalLoading: true, } );
          my.data$javaTx( `#admin 
          import com.alibaba.fastjson.JSON;
          import com.alibaba.fastjson.TypeReference;
          System.out.println("$1="+$1);
          Map<String, String> values=  JSON.parseObject($1, new TypeReference<Map<String, String>>() {});
          

          System.out.println(values);
          return true;
          `,values ).then(( result ) => {
              console.log(result);
              if ( result ) {
                  this.setState( { addUserModalVisible: false, addUserModalLoading: false } );
                  message.success( "添加成功!" )
              } else {
                  message.success( "添加失败,请重试!" )
              }
          }
          );
      } );
  };
  
//  这是原版使用mock的handleAddUserOk方法
//  handleAddUserOk = _ => {
//    const { form } = this.addUserFormRef.props;
//    form.validateFields((err, values) => {
//      if (err) {
//        return;
//      }
//      this.setState({ addUserModalLoading: true, });
//      addUser(values).then((response) => {
//        form.resetFields();
//        this.setState({ addUserModalVisible: false, addUserModalLoading: false });
//        message.success("添加成功!")
//        this.getUsers()
//      }).catch(e => {
//        message.success("添加失败,请重试!")
//      })
//    });
//  };
  
  
  componentDidMount() {
    this.getUsers()
  }
  render() {
    const { users } = this.state
    const title = (
      <span>
        <Button type='primary' onClick={this.handleAddUser}>添加用户</Button>
      </span>
    )
    const cardContent = `在这里，你可以对系统中的用户进行管理，例如添加一个新用户，或者修改系统中已经存在的用户。`
    return (
      <div className="app-container">
        <TypingCard title='用户管理' source={cardContent} />
        <br/>
        <Card title={title}>
          <Table bordered rowKey="id" dataSource={users} pagination={false}>
            <Column title="用户ID" dataIndex="id" key="id" align="center"/>
            <Column title="用户名称" dataIndex="name" key="name" align="center"/>
            <Column title="用户角色" dataIndex="role" key="role" align="center"/>
            <Column title="用户描述" dataIndex="description" key="description" align="center" />
            <Column title="操作" key="action" width={195} align="center"render={(text, row) => (
              <span>
                <Button type="primary" shape="circle" icon="edit" title="编辑" onClick={this.handleEditUser.bind(null,row)}/>
                <Divider type="vertical" />
                <Button type="primary" shape="circle" icon="delete" title="删除" onClick={this.handleDeleteUser.bind(null,row)}/>
              </span>
            )}/>
          </Table>
        </Card>
        <EditUserForm
          currentRowData={this.state.currentRowData}
          wrappedComponentRef={formRef => this.editUserFormRef = formRef}
          visible={this.state.editUserModalVisible}
          confirmLoading={this.state.editUserModalLoading}
          onCancel={this.handleCancel}
          onOk={this.handleEditUserOk}
        />  
        <AddUserForm
          wrappedComponentRef={formRef => this.addUserFormRef = formRef}
          visible={this.state.addUserModalVisible}
          confirmLoading={this.state.addUserModalLoading}
          onCancel={this.handleCancel}
          onOk={this.handleAddUserOk}
        />  
      </div>
    );
  }
}

export default User;
